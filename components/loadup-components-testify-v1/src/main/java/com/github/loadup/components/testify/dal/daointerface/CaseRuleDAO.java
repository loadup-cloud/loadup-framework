/* Copyright (C) LoadUp Cloud 2022-2025 */
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
import com.github.loadup.components.testify.dal.convertor.CaseRuleConvertor;
import com.github.loadup.components.testify.dal.dataobject.CaseRuleDO;
import com.github.loadup.components.testify.dal.table.CaseRule;
import com.github.loadup.components.testify.db.offlineService.AbstractDBService;
import com.github.loadup.components.testify.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用例规则数据表操作对象
 *
 *
 *
 */
public class CaseRuleDAO {

    /**
     * 数据库访问服务
     */
    private final AbstractDBService dbService = AbstractDBService.getService(
            TestifyDBConstants.DB_URL,
            TestifyDBConstants.DB_USER_NAME,
            TestifyDBConstants.DB_PASSWORD,
            TestifyDBConstants.DB_SCHEMA);

    /**
     * 根据系统及模型对象名查询所有的用例规则
     *
     * @param system
     * @param modelObj
     * @param status
     * @return
     */
    public List<CaseRuleDO> queryBySystemAndModelObj(String system, String methodName, String modelObj, String status) {

        List<CaseRuleDO> caseRuleDOs = new ArrayList<CaseRuleDO>();
        String querySql = "select * from case_rule where system='"
                + system
                + "' and method='"
                + methodName
                + "' and model_obj='"
                + modelObj
                + "' and status='"
                + status
                + "' order by priority";
        List<Map<String, Object>> res = dbService.executeQuerySql(querySql);
        for (Map<String, Object> row : res) {
            CaseRule caseRule = JsonUtil.genObjectFromJsonString(JSON.toJSONString(row), CaseRule.class);
            caseRuleDOs.add(CaseRuleConvertor.convert2DO(caseRule));
        }

        return caseRuleDOs;
    }
}
