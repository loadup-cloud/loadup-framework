/**
 
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.data;

/*-
 * #%L
 * loadup-components-test
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.components.testify.dal.table.CsvFillObject;
import com.github.loadup.components.testify.data.biz.BizMetaInitItemFillBuilder;
import com.github.loadup.components.testify.data.db.DbMetaInitItemFillBuilder;
import com.github.loadup.components.testify.data.enums.MatchDegree;
import com.github.loadup.components.testify.data.impl.DefaultMetaItemStore;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 原子项自动构造。
 * <p>
 * 内容来源于db 字典和业务模型。
 *
 * 
 *
 */
public class MetaItemAutoBuilder {

    /**
     * 原子初始项构造器
     */
    private MetaInitItemBuilder metaInitItemBuilder = new UnifiedMetaInitItemBuilder();

    /**
     * 自动智能匹配
     */
    private AutoMatcher autoMatcher = new AutoMatcher();

    /**
     * 原子项存储
     */
    private MetaItemStore metaItemStore = new DefaultMetaItemStore();

    /**
     * 自动匹配生成原子项。
     *
     * @param system
     */
    public void build(String system, String projectPath) {

        //第一步,先解析所有数据表,解析所有类信息,插入
        generateDataRule(system, projectPath);
        /*第二步,导入objModel文件夹的规则内容,先组装出对象关系,根据    `system`,`model_obj`,`model_data` 
                         查出source_data,然后根据source_data和system,查出org_db,然后更新*/
        updateObjDataRule(system, projectPath);
        //第三部,导入csvModel中的内容

        updateDbDataRule(system, projectPath);

    }

    /**
     * 第一步,先解析所有数据表,解析所有类信息,插入
     *
     * @param system
     * @param projectPath
     */
    public void generateDataRule(String system, String projectPath) {
        List<MetaInitItem> initItems = metaInitItemBuilder.build(system, projectPath);

        Collection<MetaItemMappingDraft> drafts = autoMatcher.match(system, initItems);

        for (MetaItemMappingDraft draft : drafts) {
            metaItemStore.addMetaItemMapping(convertDraft2Mapping(draft));
        }
    }

    /**
     * 第三步,导入dbModel文件夹的规则内容
     *
     * @param system
     * @param projectPath
     */
    public void updateDbDataRule(String system, String projectPath) {
        //第三部,导入csvModel中的内容
        DbMetaInitItemFillBuilder dbfiller = new DbMetaInitItemFillBuilder();
        List<CsvFillObject> dblist = dbfiller.build(system, projectPath);
        updateDataRule(system, dblist);
    }

    /**
     * 第二步,导入objModel文件夹的规则内容
     *
     * @param system
     * @param projectPath
     */
    public void updateObjDataRule(String system, String projectPath) {
        /*第二步,导入objModel文件夹的规则内容,先组装出对象关系,根据    `system`,`model_obj`,`model_data` 
                           查出source_data,然后根据source_data和system,查出org_db,然后更新*/

        BizMetaInitItemFillBuilder objfiller = new BizMetaInitItemFillBuilder();
        List<CsvFillObject> objlist = objfiller.build(system, projectPath);

        updateDataRule(system, objlist);
    }

    /**
     * 更新公用
     *
     * @param system
     * @param list
     */
    private void updateDataRule(String system, List<CsvFillObject> list) {
        for (CsvFillObject csvObject : list) {
            String hostName = csvObject.getModel_obj();
            String field = csvObject.getModel_data();

            MetaItemMapping metaItemMapping = metaItemStore.getMetaItemMapping(new MetaInitItem(
                    system, hostName, field));
            if (metaItemMapping == null) {

                MetaItem metaItem = new MetaItem(system, csvObject.getModel_data());
                metaItem.setDataRule(csvObject.getData_rule());
                List<String> keywords = new ArrayList<String>();
                keywords.add(csvObject.getModel_data());
                metaItem.setKeywords(keywords);

                metaItemMapping = new MetaItemMapping();
                metaItemMapping.setMetaItem(metaItem);
                metaItemMapping.addMetaInitItem(new MetaInitItem(system, hostName, field));

                metaItemStore.addMetaItemMapping(metaItemMapping);
            } else {

                MetaItem metaItem = metaItemMapping.getMetaItem();
                //如果不为空,就不要操作了
                if (StringUtils.isEmpty(metaItem.getDataRule())) {
                    metaItem.setDataRule(csvObject.getData_rule());
                    metaItemStore.updateMetaItem(metaItem);
                }
            }
        }
    }

    /**
     * draft转换。
     * <p>
     * 暂时只取建议的匹配项。
     *
     * @param draft
     * @return
     */
    private MetaItemMapping convertDraft2Mapping(MetaItemMappingDraft draft) {

        MetaItemMapping metaItemMapping = new MetaItemMapping();

        metaItemMapping.setMetaItem(draft.getMetaItem());

        for (MetaItemMappingDraft.Candidate candidate : draft.getCandidates()) {
            if (MatchDegree.RECOMMEND == candidate.matchDegree) {
                metaItemMapping.setInitItems(candidate.initItems);
            }
        }

        return metaItemMapping;
    }

    /**
     * 
     *
     * @param metaInitItemBuilder value to be assigned to property metaInitItemBuilder
     */
    public void setMetaInitItemBuilder(MetaInitItemBuilder metaInitItemBuilder) {
        this.metaInitItemBuilder = metaInitItemBuilder;
    }

}
