/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.data.impl;

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

import com.alipay.test.acts.data.MetaInitItem;
import com.alipay.test.acts.data.MetaItem;
import com.alipay.test.acts.data.MetaItemMapping;
import com.alipay.test.acts.data.MetaItemStore;
import com.alipay.test.acts.data.RuleDataStore;
import com.alipay.test.acts.exception.RuleExecuteException;

/**
 * 基于原子库获取规则。
 * 
 * @author dasong.jds
 * @version $Id: MetaItemRuleDataStore.java, v 0.1 2015年10月21日 上午10:42:03 dasong.jds Exp $
 */
public class MetaItemRuleDataStore implements RuleDataStore {

    /** 原子库 */
    private MetaItemStore metaItemStore = new DefaultMetaItemStore();

    /** 
     * @see com.alipay.acts.data.RuleDataStore#getRule(String, String)
     */
    @Override
    public String getRule(String system, String name) {
        MetaItem metaItem = metaItemStore.getMetaItem(system, name);

        if (metaItem != null) {
            return metaItem.getDataRule();
        }

        return null;
    }

    /** 
     * @see RuleDataStore#getMappedRule(String, String)
     */
    @Override
    public String getMappedRule(String system, String name) {
        if (name.lastIndexOf(".") <= 0) {
            throw new RuleExecuteException("name illegal, must like xxx.xxx[name=" + name + "]");
        }

        String[] path = name.split("\\.");
        String host = path[0];
        String field;

        for (int fieldIndex = 1; fieldIndex < path.length; fieldIndex++) {
            field = path[fieldIndex];

            MetaInitItem initItem = new MetaInitItem(system, host, field);

            MetaItemMapping mapping = metaItemStore.getMetaItemMapping(initItem);
            if (mapping != null) {
                if (fieldIndex == path.length - 1) {
                    MetaItem metaItem = mapping.getMetaItem();
                    return metaItem == null ? null : metaItem.getDataRule();
                } else {
                    initItem = mapping.getMetaInitItemOfMapping(initItem);
                    host = initItem.getFieldType().getTypeDesc();
                }
            } else {
                return null;
            }
        }

        return null;
    }
}
