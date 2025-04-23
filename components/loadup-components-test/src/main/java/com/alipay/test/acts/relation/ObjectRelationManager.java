/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.relation;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.dal.daointerface.ObjRelateDAO;
import com.alipay.test.acts.dal.daointerface.OrgDbDAO;
import com.alipay.test.acts.dal.dataobject.ObjectRelateDO;
import com.alipay.test.acts.dal.dataobject.OrgDbDO;
import com.alipay.test.acts.enums.ObjRelationFlagEnum;

/**
 * 对象关联关系管理
 * 
 * @author hongling.xiang
 * @version $Id: ObjectRelationManager.java, v 0.1 2015年9月26日 上午8:28:02 hongling.xiang Exp $
 */
public class ObjectRelationManager {

    /** 数据对象关系DAO */
    private final ObjRelateDAO objRelateDAO = new ObjRelateDAO();

    /** 原子数据对象DAO */
    private final OrgDbDAO     orgDbDAO     = new OrgDbDAO();

    /**
     * 加载指定数据模型对象关系
     * 
     * @return
     */
    public Map<String, Object> loadObjectRelation(String system, String modelObj) {

        //查询
        List<ObjectRelateDO> allObjectRelation = objRelateDAO.queryBySystemAndModelObj(system,
            modelObj);

        //构建数据对象关系
        return buildObjectRelation(system, allObjectRelation);
    }

    /**
     * 构建数据对象关系
     * 
     * @param allObjectRelation
     */
    private Map<String, Object> buildObjectRelation(String system,
                                                    List<ObjectRelateDO> allObjectRelation) {

        /** 所有数据对象关联关系集合 */
        Map<String, Object> allObjectRelationSet = new HashMap<String, Object>();

        for (ObjectRelateDO objectRelate : allObjectRelation) {
            //此对象为复杂数据对象
            if (StringUtils.equals(objectRelate.getObjFlag(),
                ObjRelationFlagEnum.COMPLEX_OBJECT.getCode())) {
                //递归构造子对象关联关系
                putSubObjectRelate(allObjectRelationSet, objectRelate, system);
            } else {
                putBaseObjectRelate(allObjectRelationSet, objectRelate);
            }
        }
        return allObjectRelationSet;
    }

    /**
     * 递归构造子对象关联关系
     * 
     * @param allObjectRelationSet
     * @param objectRelate
     */
    private void putSubObjectRelate(Map<String, Object> allObjectRelationSet,
                                    ObjectRelateDO objectRelate, String system) {
        if (allObjectRelationSet.containsKey(objectRelate.getModelData())) {
            return;
        }

        //构造子对象数据模型关系
        List<ObjectRelateDO> allSubObjectRelation = objRelateDAO.queryBySystemAndModelObj(system,
            objectRelate.getModelType());

        allObjectRelationSet.put(objectRelate.getModelData(),
            buildObjectRelation(system, allSubObjectRelation));
    }

    /**
     * 保存基本的数据模型关系
     * 
     * @param objectRelate
     */
    private void putBaseObjectRelate(Map<String, Object> allObjectRelationSet,
                                     ObjectRelateDO objectRelate) {

        if (allObjectRelationSet.containsKey(objectRelate.getModelObj())) {
            return;
        }

        OrgDbDO orgDbDO = orgDbDAO.selectBySystemAndSourceData(objectRelate.getSystem(),
            objectRelate.getSourceData());
        if (orgDbDO != null) {
            allObjectRelationSet.put(objectRelate.getModelData(), orgDbDO);
        }
    }

}
