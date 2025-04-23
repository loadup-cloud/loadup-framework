/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.datarule.handler;

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
import java.util.Map.Entry;



import com.alibaba.fastjson2.JSON;
import com.alipay.test.acts.datarule.ListChooseRule;
import com.alipay.test.acts.datarule.RULE.ReferenceHandler;
import org.apache.commons.lang3.StringUtils;

/**
 * 选择规则处理器。
 * 
 * @author zhiyuan.lzy
 * @version $Id: ListChooseRuleHandler.java, v 0.1 2015年10月24日 下午2:40:55 zhiyuan.lzy Exp $
 */
public class ListChooseRuleHandler implements RuleHandler<ListChooseRule>,
                                  BatchRuleHandler<ListChooseRule> {

    /** 
     * @see RuleHandler#handle(com.alipay.test.acts.datarule.RuleObject, ReferenceHandler)
     */

    @Override
    public String handle(ListChooseRule rule, ReferenceHandler refHandler) {

        List<Map<String, String>> rowDatas = innerHandle(rule, refHandler);

        return JSON.toJSONString(rowDatas);
    }

    /** 
     * @see BatchRuleHandler#batchHandle(com.alipay.test.acts.datarule.RuleObject, ReferenceHandler)
     */
    @Override
    public List<String> batchHandle(ListChooseRule rule, ReferenceHandler refHandler) {

        List<Map<String, String>> rowDatas = innerHandle(rule, refHandler);

        List<String> rows = new ArrayList<String>(rowDatas.size());

        for (Map<String, String> rowData : rowDatas) {
            rows.add(JSON.toJSONString(rowData));
        }

        return rows;
    }

    /**
     * list规则处理。
     * 
     * @param rule
     * @param refHandler
     * @return
     */
    private List<Map<String, String>> innerHandle(ListChooseRule rule, ReferenceHandler refHandler) {

        ChooseRuleHandler choooseRuleHandler = new ChooseRuleHandler();

        Map<String, List<String>> fieldDatas = choooseRuleHandler.getFieldData(rule.getFields(),
            refHandler);

        for (Entry<String, List<String>> entry : fieldDatas.entrySet()) {
            List<String> map = combination(entry.getValue());
            fieldDatas.put(entry.getKey(), map);
        }

        List<Map<String, String>> rowDatas;
        int count = rule.getRowCount();
        if (count <= 0) {
            rowDatas = choooseRuleHandler.enumerate(fieldDatas);
        } else {
            rowDatas = choooseRuleHandler.align(fieldDatas, count);
        }

        return rowDatas;

    }

    /***
     * 对数组元素的组合
     * 
     * @param data
     * @return
     */
    private List<String> combination(List<String> data) {

        List<String> result = new ArrayList<String>();
        int n = data.size(); //元素个数。
        //求出位图全组合的结果个数：2^n
        int nbit = 1 << n; // “<<” 表示 左移:各二进位全部左移若干位，高位丢弃，低位补0。:即求出2^n=2Bit。
        for (int i = 0; i < nbit; i++) { //结果有nbit个。输出结果从数字小到大输出：即输出0,1,2,3,....2^n。
            List<String> oneLine = new ArrayList<String>();
            for (int j = 0; j < n; j++) { //每个数二进制最多可以左移n次，即遍历完所有可能的变化新二进制数值了
                int tmp = 1 << j;
                if ((tmp & i) != 0) { //& 表示与。两个位都为1时，结果才为1
                    oneLine.add(data.get(j));
                }
            }
            if (oneLine.size() > 0) {
                result.add(StringUtils.join(oneLine, ","));
            }
        }
        return result;
    }

}
