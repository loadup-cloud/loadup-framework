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

import com.alipay.test.acts.datarule.ArrayRule;
import com.alipay.test.acts.datarule.RULE;
import com.alipay.test.acts.datarule.RULE.ReferenceHandler;
import com.alipay.test.acts.datarule.Reference;

/**
 * 序列规则处理。
 * 
 * @author dasong.jds
 * @version $Id: ArrayRuleHandler.java, v 0.1 2015年9月24日 下午2:23:15 dasong.jds Exp $
 */
public class ArrayRuleHandler implements RuleHandler<ArrayRule>, BatchRuleHandler<ArrayRule> {

    /** 
     * @see RuleHandler#handle(com.alipay.test.acts.datarule.RuleObject, ReferenceHandler)
     */
    @Override
    public String handle(ArrayRule rule, ReferenceHandler refHandler) {
        List<Object> items = rule.getItems();
        int size = items.size();
        if (size == 0) {
            return null;
        }

        int index = (int) Math.random() * size;

        return getItem(items, refHandler, index);
    }

    /** 
     * @see BatchRuleHandler#batchHandle(com.alipay.test.acts.datarule.RuleObject, ReferenceHandler)
     */
    @Override
    public List<String> batchHandle(ArrayRule rule, ReferenceHandler refHandler) {
        List<String> values = new ArrayList<String>();

        List<Object> items = rule.getItems();

        for (int i = 0; i < rule.getItems().size(); i++) {
            values.add(getItem(items, refHandler, i));
        }
        return values;
    }

    /**
     * 获取内容项。
     * 
     * @param rule
     * @param refHandler
     * @param index
     * @return
     */
    private String getItem(List<Object> items, ReferenceHandler refHandler, int index) {
        Object item = items.get(index);

        if (item instanceof Reference) {
            String name = ((Reference) item).getName();
            String value = refHandler.getData(name);
            if (value == null) {
                String refRule = refHandler.getRule(name);
                value = RULE.execute(refRule, refHandler);
                refHandler.setData(name, value);
            }
            return value;
        }
        return (String) item;
    }
}
