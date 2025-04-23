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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.alipay.test.acts.datarule.AssembleRule;
import com.alipay.test.acts.datarule.ChooseRule;
import com.alipay.test.acts.datarule.FieldRule;
import com.alipay.test.acts.datarule.RULE.ReferenceHandler;
import com.alipay.test.acts.datarule.RuleObject;
import com.alipay.test.acts.datarule.parser.RuleObjectParser;
import com.alipay.test.acts.exception.RuleExecuteException;

/**
 * 选择规则处理器。
 * 
 * @author dasong.jds
 * @version $Id: ChooseRuleHandler.java, v 0.1 2015年10月15日 下午2:40:55 dasong.jds Exp $
 */
public class ChooseRuleHandler implements RuleHandler<ChooseRule> {

    /** 
     * @see RuleHandler#handle(RuleObject, ReferenceHandler)
     */

    @Override
    public String handle(ChooseRule rule, ReferenceHandler refHandler) {

        Map<String, List<String>> fieldDatas = getFieldData(rule.getFields(), refHandler);

        List<Map<String, String>> rowDatas;
        int count = rule.getRowCount();
        if (count <= 0) {
            rowDatas = fullChoose(rule, refHandler, fieldDatas);
        } else {
            rowDatas = partChoose(rule, refHandler, fieldDatas, count);
        }

        return JSON.toJSONString(rowDatas);
    }

    /**
     * 部分选取。
     * 
     * @param rule
     * @param refHandler
     * @param fieldDatas
     * @param count
     * @return
     */
    private List<Map<String, String>> partChoose(ChooseRule rule, ReferenceHandler refHandler,
                                                 Map<String, List<String>> fieldDatas, int count) {

        List<Map<String, String>> rowDatas = new ArrayList<Map<String, String>>();

        List<Map<String, String>> fieldRowDatas = align(fieldDatas, count);

        // 逐行处理assemble
        for (Map<String, String> row : fieldRowDatas) {
            List<List<Map<String, String>>> assembleDatas = handleAssembleRule(
                rule.getAssembleRules(), refHandler, row);

            // 全排列然后随机取出1条做连接
            if (!assembleDatas.isEmpty()) {
                List<Map<String, String>> assembleRowDatas = new ArrayList<Map<String, String>>();

                List<Map<String, String>> rowData = new ArrayList<Map<String, String>>(1);
                rowData.add(row);
                for (List<Map<String, String>> assembleData : assembleDatas) {
                    assembleRowDatas.addAll(join(rowData, assembleData));
                }
                rowDatas.addAll(getSpecifyItems(1, assembleRowDatas));
            } else {
                rowDatas.add(row);
            }
        }
        return rowDatas;
    }

    /**
     * 全量选取。
     * 
     * @param rule
     * @param refHandler
     * @param fieldDatas
     * @return
     */
    private List<Map<String, String>> fullChoose(ChooseRule rule, ReferenceHandler refHandler,
                                                 Map<String, List<String>> fieldDatas) {

        List<Map<String, String>> rowDatas = new ArrayList<Map<String, String>>();

        // 全排列
        List<Map<String, String>> fieldRowDatas = enumerate(fieldDatas);

        // 逐行处理assemble
        for (Map<String, String> row : fieldRowDatas) {
            // 计算组装值
            List<List<Map<String, String>>> assembleDatas = handleAssembleRule(
                rule.getAssembleRules(), refHandler, row);

            // 全排列组装值
            if (!assembleDatas.isEmpty()) {
                List<Map<String, String>> rowData = new ArrayList<Map<String, String>>(1);
                rowData.add(row);
                for (List<Map<String, String>> assembleData : assembleDatas) {
                    rowDatas.addAll(join(rowData, assembleData));
                }
            } else {
                rowDatas.add(row);
            }
        }
        return rowDatas;
    }

    /**
     * 处理组装规则。
     * 
     * @param assembleRules
     * @param refHandler
     * @param datas
     * @return
     */
    List<List<Map<String, String>>> handleAssembleRule(List<AssembleRule> assembleRules,
                                                       final ReferenceHandler refHandler,
                                                       final Map<String, String> datas) {
        if (assembleRules == null || assembleRules.isEmpty()) {
            return Collections.emptyList();
        }

        // 优先从当前生成数据中获取值
        ReferenceHandler handlerProxy = new ReferenceHandler() {

            @Override
            public String getSystem() {
                return refHandler.getSystem();
            }

            @Override
            public String getRule(String name) {
                return refHandler.getRule(name);
            }

            @Override
            public String getData(String name) {
                if (datas.containsKey(name)) {
                    return datas.get(name);
                }
                return refHandler.getData(name);
            }

            @Override
            public void setData(String name, String data) {
                refHandler.setData(name, data);
            }
        };

        List<List<Map<String, String>>> rowDatas = new ArrayList<List<Map<String, String>>>(
            assembleRules.size());
        for (AssembleRule rule : assembleRules) {
            List<Map<String, String>> assembleData = new AssembleRuleHandler().innerHandle(rule,
                handlerProxy);
            if (!assembleData.isEmpty()) {
                rowDatas.add(assembleData);
            }
        }
        return rowDatas;
    }

    /**
     * 按指定行数对齐数据。
     * 
     * @param fieldDatas
     * @param count
     * @return
     */
    List<Map<String, String>> align(Map<String, List<String>> fieldDatas, int count) {
        // 对齐数据
        Map<String, List<String>> alignedDatas = new HashMap<String, List<String>>();
        for (String key : fieldDatas.keySet()) {
            alignedDatas.put(key, getSpecifyItems(count, fieldDatas.get(key)));
        }
        // 行列转换
        List<Map<String, String>> fieldRowDatas = rcTransfer(alignedDatas);
        return fieldRowDatas;
    }

    /**
     * 行列转换。
     * 
     * @param datas
     * @return
     */
    private List<Map<String, String>> rcTransfer(Map<String, List<String>> datas) {
        List<Map<String, String>> rowDatas = new ArrayList<Map<String, String>>();

        for (String key : datas.keySet()) {
            directLink(rowDatas, key, datas.get(key));
        }

        return rowDatas;
    }

    /**
     * 直连接。
     * 
     * @param rowDatas
     * @param key
     * @param values
     */
    private void directLink(List<Map<String, String>> rowDatas, String key, List<String> values) {

        for (int i = 0; i < values.size(); i++) {
            if (i == rowDatas.size()) {
                rowDatas.add(new HashMap<String, String>());
            }
            rowDatas.get(i).put(key, values.get(i));
        }
    }

    /**
     * 全排列。
     * <p>
     * a={a1,a2}<br>
     * b={b1,b2}<br>
     * 全排列成：<br>
     * {a:a1,b:b1},{a:a2,b:b1},{a:a1,b:b2},{a:a2,b:b2}
     * 
     * @param datas
     * @return
     */
    List<Map<String, String>> enumerate(Map<String, List<String>> datas) {
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
     * @param right
     * @return
     */
    private List<Map<String, String>> join(List<Map<String, String>> left,
                                           List<Map<String, String>> right) {
        List<Map<String, String>> rows = new ArrayList<Map<String, String>>(left.size()
                                                                            * right.size());

        for (Map<String, String> l : left) {
            for (Map<String, String> r : right) {
                Map<String, String> newMap = new HashMap<String, String>();
                newMap.putAll(l);
                newMap.putAll(r);
                rows.add(newMap);
            }
        }

        return rows;
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
     * 获取指定个数的内容。
     * 
     * @param count
     * @param items
     * @return
     */
    private <T> List<T> getSpecifyItems(int count, List<T> items) {
        int size = items.size();

        if (count <= 0 || count == size) {
            return items;
        }

        List<T> sItems = new ArrayList<T>(count);
        if (count > size) {
            for (int i = 0; i < count; i++) {
                sItems.add(items.get(i % size));
            }

            return sItems;
        }

        int[] indexes = random(count, size);
        for (int i = 0; i < count; i++) {
            sItems.add(items.get(indexes[i]));
        }

        return sItems;

    }

    /**
     * 获取指定范围的随机数。
     * 
     * @param count
     * @param size
     * @return
     */
    private int[] random(int count, int size) {

        int[] indexes = new int[count];

        for (int i = 0; i < count;) {
            int r = (int) (Math.random() * size);

            boolean isExist = false;
            for (int j = 0; j < i; j++) {
                if (indexes[j] == r) {
                    isExist = true;
                    break;
                }
            }

            if (isExist) {
                continue;
            } else {
                indexes[i] = r;
                i++;
            }
        }

        return indexes;
    }

    /**
     * 获取各维度全量数据。
     * 
     * @param fields
     * @param refHandler
     * @return
     */
    @SuppressWarnings("unchecked")
    Map<String, List<String>> getFieldData(List<FieldRule> fields, ReferenceHandler refHandler) {
        Map<String, List<String>> datas = new HashMap<String, List<String>>();

        for (FieldRule field : fields) {

            RuleObject ruleObject = field.getRule();

            if (ruleObject == null) {
                String r = refHandler.getRule(field.getFieldName());
                if (StringUtils.isEmpty(r)) {
                    throw new RuleExecuteException("can't find field rule[field="
                                                   + field.getFieldName() + "]");
                }
                ruleObject = new RuleObjectParser(r).parse();
            }

            List<String> data = RuleHandlerFactory.getBatchHandler(ruleObject.getClass())
                .batchHandle(ruleObject, refHandler);

            datas.put(field.getFieldName(), data);
        }

        return datas;
    }
}
