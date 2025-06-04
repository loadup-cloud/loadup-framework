/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.data.db;

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

import com.github.loadup.components.testify.constant.TestifyDBConstants;
import com.github.loadup.components.testify.data.MetaFieldType;
import com.github.loadup.components.testify.data.MetaInitItem;
import com.github.loadup.components.testify.data.MetaItem;
import com.github.loadup.components.testify.data.MetaItemMapping;
import com.github.loadup.components.testify.data.enums.MetaInitType;
import com.github.loadup.components.testify.db.offlineService.AbstractDBService;
import com.github.loadup.components.testify.exception.RuleExecuteException;
import com.github.loadup.components.testify.manage.TestLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * 原子项db仓储。
 *
 *
 *
 */
public class MetaItemRepository {

    private static final AbstractDBService dbService;

    static {
        AbstractDBService dbs = null;
        try {
            dbs = AbstractDBService.getService(
                    TestifyDBConstants.DB_URL,
                    TestifyDBConstants.DB_USER_NAME,
                    TestifyDBConstants.DB_PASSWORD,
                    TestifyDBConstants.DB_SCHEMA);
        } catch (Exception e) {
            dbs = null;
            TestLogger.getLogger().error(e, "db access error,url=" + TestifyDBConstants.DB_URL);
        }

        dbService = dbs;
    }

    private final String SQL_GET_META_ITEMS = "select source_data from org_db where system='?'";

    private final String SQL_GET_META_ITEM =
            "select system,source_data,keywords,source_rule from org_db where system='?' and source_data='?'";

    private final String SQL_GET_META_INIT_ITEM =
            "select system,category,model_obj,model_data,model_type from obj_relate where system='?' and model_obj='?' and model_data='?'";

    private final String SQL_GET_META_ITEM_MAPPING_BY_META =
            "select system,category,model_obj,model_data,model_type,source_data from obj_relate where system='?' and source_data='?'";

    private final String SQL_GET_META_ITEM_MAPPING_BY_INIT =
            "select system,category,model_obj,model_data,model_type,source_data from obj_relate where system='?' and model_obj='?' and model_data='?'";

    private final String SQL_INSERT_META_ITEM =
            "insert into org_db(system,source_data,keywords,source_rule,source_id) values('?','?','?','?','?')";

    private final String SQL_INSERT_META_INIT_ITEM =
            "insert into obj_relate(system,category,model_obj,model_data,model_type,id) values('?','?','?','?','?','?')";

    private final String SQL_INSERT_META_ITEM_MAPPING =
            "insert into obj_relate(system,category,model_obj,model_data,source_data,model_type,id) values('?','?','?','?','?','?','?')";

    private final String SQL_UPDATE_META_ITEM_MAPPING =
            "update obj_relate set source_data='?' where system='?' and model_obj='?' and model_data='?'";

    private final String SQL_UPDATE_META_ITEM =
            "update org_db set keywords='?', source_rule='?' where system='?' and source_data='?'";

    private final String SQL_UPDATE_META_INIT_ITEM =
            "update obj_relate set model_type='?',category='?' where system='?' and model_obj='?' and model_data='?'";

    /**
     * 获取所有原子项。
     *
     * @param system
     * @return
     */
    public List<String> getMetaItemIds(String system) {

        String sql = paramReplace(SQL_GET_META_ITEMS, system);

        List<Map<String, Object>> result = getDbService().executeQuerySql(sql);

        List<String> list = new ArrayList<String>();
        if (result.size() > 0) {
            for (Map<String, Object> m : result) {
                list.add((String) m.get("source_data"));
            }
        }
        return list;
    }

    /**
     * 加载原子项。
     *
     * @param system
     * @param id
     * @return
     */
    public MetaItem loadMetaItem(String system, String id) {
        String sql = paramReplace(SQL_GET_META_ITEM, system, id);

        List<Map<String, Object>> result = getDbService().executeQuerySql(sql);

        if (result.size() > 0) {

            Map<String, Object> entry = result.get(0);
            MetaItem metaItem = new MetaItem((String) entry.get("system"), (String) entry.get("source_data"));

            metaItem.setDataRule((String) entry.get("source_rule"));

            String keywords = (String) entry.get("keywords");
            String[] kws = keywords.split(";");
            for (String k : kws) {
                metaItem.addKeyword(k);
            }

            return metaItem;
        }
        return null;
    }

    /**
     * 加载初始项。
     *
     * @param system
     * @param host
     * @param field
     * @return
     */
    public MetaInitItem loadMetaInitItem(String system, String host, String field) {

        String sql = paramReplace(SQL_GET_META_INIT_ITEM, system, host, field);

        List<Map<String, Object>> result = getDbService().executeQuerySql(sql);

        if (result.size() > 0) {
            Map<String, Object> entry = result.get(0);
            MetaInitItem metaInitItem = composeMetaInitItem(entry);

            return metaInitItem;
        }
        return null;
    }

    /**
     * 加载映射关系。
     *
     * @param system
     * @param metaItemId
     * @return
     */
    public MetaItemMapping loadMetaItemMapping(String system, String metaItemId) {

        MetaItem metaItem = loadMetaItem(system, metaItemId);

        if (metaItem == null) {
            return null;
        }

        String sql = paramReplace(SQL_GET_META_ITEM_MAPPING_BY_META, system, metaItemId);

        List<Map<String, Object>> result = getDbService().executeQuerySql(sql);

        MetaItemMapping metaItemMapping = new MetaItemMapping();
        metaItemMapping.setMetaItem(metaItem);
        if (result.size() > 0) {
            for (Map<String, Object> m : result) {
                MetaInitItem initItem = composeMetaInitItem(m);

                metaItemMapping.addMetaInitItem(initItem);
            }
        }

        return metaItemMapping;
    }

    /**
     * 加载映射项。
     *
     * @param system
     * @param hostName
     * @param field
     * @return
     */
    public MetaItemMapping loadMetaItemMapping(String system, String hostName, String field) {

        String sql = paramReplace(SQL_GET_META_ITEM_MAPPING_BY_INIT, system, hostName, field);

        List<Map<String, Object>> result = getDbService().executeQuerySql(sql);

        if (result.size() > 0) {

            Map<String, Object> entry = result.get(0);

            return loadMetaItemMapping((String) entry.get("system"), (String) entry.get("source_data"));
        }

        return null;
    }

    /**
     * 保存原子项。
     *
     * @param metaItem
     */
    public void storeMetaItem(MetaItem metaItem) {
        String keywords = StringUtils.join(metaItem.getKeywords().iterator(), ";");
        String sql = paramReplace(
                SQL_INSERT_META_ITEM,
                metaItem.getSystem(),
                metaItem.getId(),
                keywords,
                metaItem.getDataRule(),
                metaItem.getSystem() + "_" + metaItem.getId());

        getDbService().executeUpdateSql(sql);
    }

    /**
     * 更新原子项。
     *
     * @param metaItem
     */
    public int reStoreMetaItem(MetaItem metaItem) {
        String keywords = StringUtils.join(metaItem.getKeywords().iterator(), ";");
        String sql = paramReplace(
                SQL_UPDATE_META_ITEM, keywords, metaItem.getDataRule(), metaItem.getSystem(), metaItem.getId());

        int i = getDbService().executeUpdateSql(sql);
        return i;
    }

    /**
     * 保存初始项。
     *
     * @param initItem
     */
    public void storeMetaInitItem(MetaInitItem initItem) {

        String sql = paramReplace(
                SQL_INSERT_META_INIT_ITEM,
                initItem.getSystem(),
                initItem.getHost(),
                initItem.getField(),
                initItem.getInitType().name(),
                initItem.getFieldType().getTypeDesc());

        getDbService().executeUpdateSql(sql);
    }

    /**
     * 更新初始项。
     *
     * @param metaInitItem
     */
    public void reStoreMetaInitItem(MetaInitItem metaInitItem) {
        String sql = paramReplace(
                SQL_UPDATE_META_INIT_ITEM,
                metaInitItem.getFieldType().getTypeDesc(),
                metaInitItem.getInitType().name(),
                metaInitItem.getSystem(),
                metaInitItem.getHost(),
                metaInitItem.getField());

        getDbService().executeUpdateSql(sql);
    }

    /**
     * 保存映射关系。
     *
     * @param metaItemMapping
     */
    public void storeMetaItemMapping(MetaItemMapping metaItemMapping) {

        MetaItem metaItem = loadMetaItem(
                metaItemMapping.getMetaItem().getSystem(),
                metaItemMapping.getMetaItem().getId());
        if (metaItem == null) {
            metaItem = metaItemMapping.getMetaItem();
            storeMetaItem(metaItem);
        }

        for (MetaInitItem initItem : metaItemMapping.getInitItems()) {
            MetaInitItem im = loadMetaInitItem(initItem.getSystem(), initItem.getHost(), initItem.getField());
            if (im == null) {
                im = initItem;
                storeMetaInitItem(im);
            }

            MetaItemMapping mapping =
                    loadMetaItemMapping(initItem.getSystem(), initItem.getHost(), initItem.getField());

            if (mapping == null) {
                String insertMappingSql = paramReplace(
                        SQL_INSERT_META_ITEM_MAPPING,
                        metaItem.getSystem(),
                        "",
                        initItem.getHost(),
                        initItem.getField(),
                        metaItem.getId(),
                        initItem.getFieldType().getTypeDesc(),
                        metaItem.getSystem() + "_" + initItem.getHost() + "_" + initItem.getField());

                getDbService().executeUpdateSql(insertMappingSql);
            } else {
                String updateMappingSql = paramReplace(
                        SQL_UPDATE_META_ITEM_MAPPING,
                        metaItem.getId(),
                        metaItem.getSystem(),
                        initItem.getHost(),
                        initItem.getField());

                getDbService().executeUpdateSql(updateMappingSql);
            }
        }
    }

    /**
     * 参数替换。
     *
     * @param sql
     * @param params
     * @return
     */
    private String paramReplace(String sql, String... params) {

        String newSql = sql;
        for (String param : params) {
            if (param == null) {
                param = "";
            }
            newSql = newSql.replaceFirst("\\?", param);
        }
        return newSql;
    }

    /**
     * 初始项设置值。
     *
     * @param entry
     */
    private MetaInitItem composeMetaInitItem(Map<String, Object> entry) {
        MetaInitItem metaInitItem = new MetaInitItem(
                (String) entry.get("system"), (String) entry.get("model_obj"), (String) entry.get("model_data"));

        String category = (String) entry.get("category");
        if (!StringUtils.isEmpty(category)) {
            metaInitItem.setInitType(MetaInitType.valueOf(category));
        }
        metaInitItem.setFieldType(new MetaFieldType((String) entry.get("model_type")));

        return metaInitItem;
    }

    /**
     * 获取db服务。
     *
     * @return
     */
    private AbstractDBService getDbService() {
        if (dbService == null) {
            throw new RuleExecuteException("db access error, can't get db service");
        }
        return dbService;
    }
}
