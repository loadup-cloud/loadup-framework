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
import com.github.loadup.components.testify.dal.convertor.OrgDbConvertor;
import com.github.loadup.components.testify.dal.dataobject.OrgDbDO;
import com.github.loadup.components.testify.dal.table.OrgDb;
import com.github.loadup.components.testify.db.offlineService.AbstractDBService;
import com.github.loadup.components.testify.util.JsonUtil;
import org.springframework.dao.DataAccessException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 原子数据对象DAO操作
 *
 * 
 *
 */
public class OrgDbDAO {

    private final AbstractDBService dbService = AbstractDBService.getService(
            TestifyDBConstants.DB_URL,
            TestifyDBConstants.DB_USER_NAME,
            TestifyDBConstants.DB_PASSWORD,
            TestifyDBConstants.DB_SCHEMA);

    /**
     * 根据系统名及属性字段查询原子数据对象
     *
     * @param system
     * @param sourceData
     * @return
     * @throws DataAccessException
     */
    public OrgDbDO selectBySystemAndSourceData(String system, String sourceData) {

        String querySql = "select * from org_db where system='" + system + "' and source_data='"
                + sourceData + "'";
        List<Map<String, Object>> res = dbService.executeQuerySql(querySql);
        if (res.size() != 1) {
            return null;
        }
        return OrgDbConvertor.convert2DO(JsonUtil.genObjectFromJsonString(
                JSON.toJSONString(res.get(0)), OrgDb.class));
    }

    /**
     * 根据ID查询原子数据对象
     *
     * @param sourceId
     * @return
     * @throws DataAccessException
     */
    public OrgDbDO selectBySourceId(String sourceId) {

        String querySql = "select * from org_db where source_id='" + sourceId + "'";
        List<Map<String, Object>> res = dbService.executeQuerySql(querySql);
        if (res.size() != 1) {
            throw new Error("存在多条数据");
        }
        return OrgDbConvertor.convert2DO(JsonUtil.genObjectFromJsonString(
                JSON.toJSONString(res.get(0)), OrgDb.class));
    }

    /**
     * 查询指定系统的所有数据模型
     *
     * @param system
     * @return
     * @throws DataAccessException
     */
    public List<OrgDbDO> selectBySystem(String system) {

        List<OrgDbDO> orgDbDOs = new ArrayList<OrgDbDO>();

        String querySql = "select * from org_db where system='" + system + "'";
        List<Map<String, Object>> res = dbService.executeQuerySql(querySql);
        for (Map<String, Object> row : res) {
            orgDbDOs.add(OrgDbConvertor.convert2DO(JsonUtil.genObjectFromJsonString(
                    JSON.toJSONString(row), OrgDb.class)));
        }

        return orgDbDOs;
    }

    /**
     * 插入
     *
     * @param system
     * @return
     * @throws DataAccessException
     */
    public int insert(OrgDbDO orgDbDO) {

        String system = orgDbDO.getSystem();
        String sourceData = orgDbDO.getSourceData();
        String sourceId = orgDbDO.getSourceId();
        Timestamp tamp = new Timestamp(orgDbDO.getGmtCreate().getTime());
        String gmtCreate = tamp.toString();
        tamp.setTime(orgDbDO.getGmtModify().getTime());
        String gmtModify = tamp.toString();
        String querySql = "INSERT INTO org_db (`source_id`, `system`, `source_data`, `source_rule`, `gmt_create`, `gmt_modify`, `memo`, `keywords`) VALUES ('"
                + sourceId
                + "', '"
                + system
                + "', '"
                + sourceData
                + "', '', '"
                + gmtCreate + "', '" + gmtModify + "', NULL, '" + sourceData + "');";
        int i = dbService.executeUpdateSql(querySql);
        return i;
    }
}
