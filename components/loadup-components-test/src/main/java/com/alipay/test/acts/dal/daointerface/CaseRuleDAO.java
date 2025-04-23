/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.dal.daointerface;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.alipay.test.acts.constant.ActsDBConstants;
import com.alipay.test.acts.dal.convertor.CaseRuleConvertor;
import com.alipay.test.acts.dal.dataobject.CaseRuleDO;
import com.alipay.test.acts.dal.table.CaseRule;
import com.alipay.test.acts.db.offlineService.AbstractDBService;
import com.alipay.test.acts.util.JsonUtil;

/**
 * 用例规则数据表操作对象
 * 
 * @author hongling.xiang
 * @version $Id: CaseRuleDAO.java, v 0.1 2015年10月15日 上午22:53:08 hongling.xiang Exp $
 */
public class CaseRuleDAO {

    /** 数据库访问服务 */
    private final AbstractDBService dbService = AbstractDBService.getService(
                                                  ActsDBConstants.DB_URL,
                                                  ActsDBConstants.DB_USER_NAME,
                                                  ActsDBConstants.DB_PASSWORD,
                                                  ActsDBConstants.DB_SCHEMA);

    /**
     * 根据系统及模型对象名查询所有的用例规则
     * 
     * @param system
     * @param modelObj
     * @param status
     * @return
     */
    public List<CaseRuleDO> queryBySystemAndModelObj(String system, String methodName,
                                                     String modelObj, String status) {

        List<CaseRuleDO> caseRuleDOs = new ArrayList<CaseRuleDO>();
        String querySql = "select * from case_rule where system='" + system + "' and method='"
                          + methodName + "' and model_obj='" + modelObj + "' and status='" + status
                          + "' order by priority";
        List<Map<String, Object>> res = dbService.executeQuerySql(querySql);
        for (Map<String, Object> row : res) {
            CaseRule caseRule = JsonUtil.genObjectFromJsonString(JSON.toJSONString(row),
                CaseRule.class);
            caseRuleDOs.add(CaseRuleConvertor.convert2DO(caseRule));
        }

        return caseRuleDOs;
    }

}
