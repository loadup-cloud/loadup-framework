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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;



import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.alipay.test.acts.datarule.AssembleCondition;
import com.alipay.test.acts.datarule.AssembleConditionItem;
import com.alipay.test.acts.datarule.AssembleConditionOperator;
import com.alipay.test.acts.datarule.AssembleItem;
import com.alipay.test.acts.datarule.AssembleRule;
import com.alipay.test.acts.datarule.AssembleValue;
import com.alipay.test.acts.datarule.RULE.ReferenceHandler;
import com.alipay.test.acts.datarule.RuleObject;
import com.alipay.test.acts.exception.RuleExecuteException;

/**
 * 组装规则处理器。
 * 
 * @author dasong.jds
 * @version $Id: AssembleRuleHandler.java, v 0.1 2015年10月26日 下午2:57:26 dasong.jds Exp $
 */
public class AssembleRuleHandler implements RuleHandler<AssembleRule> {

    /** 
     * @see RuleHandler#handle(RuleObject, ReferenceHandler)
     */
    @Override
    public String handle(AssembleRule rule, ReferenceHandler refHandler) {

        List<Map<String, String>> records = innerHandle(rule, refHandler);

        return JSON.toJSONString(records);
    }

    /**
     * 内部使用。
     * 
     * @param rule
     * @param refHandler
     * @return
     */
    public List<Map<String, String>> innerHandle(AssembleRule rule, ReferenceHandler refHandler) {

        List<Map<String, String>> records = new ArrayList<Map<String, String>>();

        for (AssembleItem item : rule.getItems()) {
            Map<String, String> condtionValues = new HashMap<String, String>();

            if (meetCondition(item.getCondition(), condtionValues, refHandler)) {

                Map<String, List<String>> datas = getDemensionData(item, refHandler);

                List<Map<String, String>> rowDatas = enumerate(datas);

                for (Map<String, String> rowData : rowDatas) {
                    rowData.putAll(condtionValues);
                }

                records.addAll(rowDatas);
            }
        }

        return records;
    }

    /**
     * 条件处理。
     * 
     * @param condition
     * @param values
     * @param refHandler
     * @return
     */
    private boolean meetCondition(AssembleCondition condition, Map<String, String> values,
                                  ReferenceHandler refHandler) {
        if (condition == null) {
            return true;
        }
        List<Object> items = condition.getItems();

        Stack<Object> operateStack = new Stack<Object>();
        for (Object item : items) {
            if (item instanceof AssembleConditionItem) {
                boolean thiz = executeConditionItem((AssembleConditionItem) item, values,
                    refHandler);

                if (operateStack.isEmpty()
                    || operateStack.peek() == AssembleConditionOperator.LEFT_PAREN) {
                    operateStack.push(thiz);
                } else {
                    AssembleConditionOperator operator = (AssembleConditionOperator) operateStack
                        .peek();
                    if (AssembleConditionOperator.AND == operator) {
                        operateStack.pop();
                        Boolean that = (Boolean) operateStack.pop();
                        operateStack.push(that && thiz);
                    } else {
                        operateStack.push(thiz);
                    }
                }
            } else if (item instanceof AssembleConditionOperator) {
                if (AssembleConditionOperator.RIGHT_PAREN == item) {

                    Boolean bool = null;
                    for (Object element = operateStack.pop(); element != AssembleConditionOperator.LEFT_PAREN; element = operateStack
                        .pop()) {
                        if (element instanceof Boolean) {
                            bool = (Boolean) element;
                        } else if (element == AssembleConditionOperator.OR) {
                            bool = (Boolean) operateStack.pop() || bool;
                        }
                    }
                    if (bool != null) {
                        operateStack.push(bool);
                    }

                } else {
                    operateStack.push(item);
                }
            }
        }

        while (!operateStack.isEmpty()) {
            Boolean thiz = (Boolean) operateStack.pop();
            AssembleConditionOperator operator = null;
            if (!operateStack.isEmpty()) {
                operator = (AssembleConditionOperator) operateStack.pop();
                Boolean that = (Boolean) operateStack.pop();
                if (AssembleConditionOperator.AND == operator) {
                    operateStack.push(thiz && that);
                } else {
                    operateStack.push(thiz || that);
                }
            } else {
                return thiz;
            }
        }

        return false;
    }

    /**
     * 条件项执行。
     * 
     * @param item
     * @param values
     * @param refHandler
     * @return
     */
    private boolean executeConditionItem(AssembleConditionItem item, Map<String, String> values,
                                         ReferenceHandler refHandler) {
        if (item == null) {
            return true;
        }

        String field = item.getField();
        String realValue = refHandler.getData(field);
        String expectValue = item.getValue();
        AssembleConditionOperator operator = item.getOperator();

        boolean isOk = compare(realValue, expectValue, operator);
        if (isOk) {
            values.put(field, realValue);
        }
        return isOk;
    }

    /**
     * 比较运算。
     * 
     * @param realValue
     * @param expectValue
     * @param operator
     * @return
     */
    private boolean compare(String realValue, String expectValue, AssembleConditionOperator operator) {
        switch (operator) {
            case EQUAL:
                if (StringUtils.isEmpty(realValue)) {
                    return "null".equals(expectValue);
                } else {
                    return StringUtils.equals(realValue, expectValue);
                }
            case GREATER:
                return realValue == null ? false : expectValue.compareTo(realValue) < 0;
            case GREATER_EQUAL:
                return realValue == null ? false : expectValue.compareTo(realValue) <= 0;
            case LESS:
                return realValue == null ? false : expectValue.compareTo(realValue) > 0;
            case LESS_EQUAL:
                return realValue == null ? false : expectValue.compareTo(realValue) >= 0;
            case NOT_EQUAL:
                return !StringUtils.equals(realValue, expectValue);
            default:
                throw new RuleExecuteException("not support operator:" + operator);
        }
    }

    /**
     * 全排列。
     * 
     * @param datas
     * @return
     */
    private List<Map<String, String>> enumerate(Map<String, List<String>> datas) {
        List<Map<String, String>> tmp = new ArrayList<Map<String, String>>(1);
        tmp.add(new HashMap<String, String>());

        for (String key : datas.keySet()) {
            tmp = join(tmp, key, datas.get(key));
        }

        return tmp;
    }

    /**
     * 全连接。
     * 
     * @param left
     * @param key
     * @param values
     * @return
     */
    private List<Map<String, String>> join(List<Map<String, String>> left, String key,
                                           List<String> values) {

        List<Map<String, String>> rows = new ArrayList<Map<String, String>>(left.size()
                                                                            * values.size());
        for (Map<String, String> m : left) {
            for (String v : values) {
                Map<String, String> newMap = new HashMap<String, String>();
                newMap.putAll(m);
                newMap.put(key, v);
                rows.add(newMap);
            }
        }
        return rows;
    }

    /**
     * 获取各维度全量数据。
     * 
     * @param item
     * @param refHandler
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<String>> getDemensionData(AssembleItem item,
                                                       ReferenceHandler refHandler) {
        Map<String, List<String>> datas = new HashMap<String, List<String>>();

        for (AssembleValue value : item.getValues()) {
            String field = value.getField();
            RuleObject ruleObject = value.getRule();

            List<String> data = RuleHandlerFactory.getBatchHandler(ruleObject.getClass())
                .batchHandle(ruleObject, refHandler);

            datas.put(field, data);
        }

        return datas;
    }
}
