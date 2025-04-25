/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.dal.daointerface;

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

import com.alibaba.fastjson2.JSON;
import com.github.loadup.components.testify.constant.TestifyDBConstants;
import com.github.loadup.components.testify.dal.convertor.ObjectRelateConvertor;
import com.github.loadup.components.testify.dal.dataobject.ObjectRelateDO;
import com.github.loadup.components.testify.dal.table.ObjectRelate;
import com.github.loadup.components.testify.db.offlineService.AbstractDBService;
import com.github.loadup.components.testify.util.JsonUtil;
import org.springframework.dao.DataAccessException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据对象关联关系DAO
 *
 * 
 *
 */
public class ObjRelateDAO {

    private final AbstractDBService dbService = AbstractDBService.getService(
            TestifyDBConstants.DB_URL,
            TestifyDBConstants.DB_USER_NAME,
            TestifyDBConstants.DB_PASSWORD,
            TestifyDBConstants.DB_SCHEMA);

    /**
     * 根据系统及模型对象名查询对象关联关系
     *
     * @param system
     * @param modelObj
     * @return
     */
    public List<ObjectRelateDO> queryBySystemAndModelObj(String system, String modelObj) {

        List<ObjectRelateDO> objectRelateDOs = new ArrayList<ObjectRelateDO>();
        String querySql = "select * from obj_relate where system='" + system + "' and model_obj='"
                + modelObj + "'";
        List<Map<String, Object>> res = dbService.executeQuerySql(querySql);
        for (Map<String, Object> row : res) {
            ObjectRelate objectRelate = JsonUtil.genObjectFromJsonString(JSON.toJSONString(row),
                    ObjectRelate.class);
            objectRelateDOs.add(ObjectRelateConvertor.convert2DO(objectRelate));
        }

        return objectRelateDOs;
    }

    /**
     * 插入
     *
     * @param system
     * @return
     * @throws DataAccessException
     */
    public int insert(ObjectRelateDO objectRelateDO) {

        String id = objectRelateDO.getId();
        String system = objectRelateDO.getSystem();
        String modelObj = objectRelateDO.getModelObj();
        String modelData = objectRelateDO.getModelData();
        String modelType = objectRelateDO.getModelType();
        String sourceData = objectRelateDO.getSourceData();
        String relateSourceId = objectRelateDO.getRelateSourceId();
        String objFlag = objectRelateDO.getObjFlag();

        Timestamp tamp = new Timestamp(objectRelateDO.getGmtCreate().getTime());
        String gmtCreate = tamp.toString();
        tamp.setTime(objectRelateDO.getGmtModify().getTime());
        String gmtModify = tamp.toString();
        String executeSql = "INSERT INTO `acts`.`obj_relate` (`id`, `system`, `model_obj`, `model_data`, `model_type`, `data_dsc`, `obj_flag`, `relate_source_id`, `source_data`, `gmt_create`, `gmt_modify`, `memo`) VALUES ('"
                + id
                + "', '"
                + system
                + "', '"
                + modelObj
                + "', '"
                + modelData
                + "', '"
                + modelType
                + "', '', '"
                + objFlag
                + "', '"
                + relateSourceId
                + "', '"
                + sourceData
                + "', '"
                + gmtCreate
                + "', '"
                + gmtModify
                + "', NULL);";
        int i = dbService.executeUpdateSql(executeSql);
        return i;
    }
}
