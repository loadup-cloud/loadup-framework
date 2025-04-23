/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.datarule;

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

/**
 * 组装规则。
 * 
 * <p>
 * 根据条件组装特定数据集合。<br>
 * 规则形式：<br>
 * assemble(aa=aa1&bb=bb2{cc:{cc1,cc2,cc3},dd:{dd1}},{ee:{ee1},ff:{ff2}})
 * 
 * <br>
 * 返回结果：若上下文中，aa=aa1&bb=bb2 则返回aa1,bb2,(cc1,cc2,cc3),dd1,ee1,ff2，共3条记录
 * 否则，则返回aa,bb,cc,dd当前值 + ee1,ff2
 * 
 * @author dasong.jds
 * @version $Id: AssembleRule.java, v 0.1 2015年10月25日 上午10:48:38 dasong.jds Exp $
 */
public class AssembleRule implements RuleObject {

    /** 组装内容 */
    private List<AssembleItem> items = new ArrayList<AssembleItem>();

    /**
     * 增加组装内容。
     * 
     * @param item
     */
    public void addItem(AssembleItem item) {
        this.items.add(item);
    }

    /**
     * Getter method for property <tt>items</tt>.
     * 
     * @return property value of items
     */
    public List<AssembleItem> getItems() {
        return items;
    }

    /**
     * Setter method for property <tt>items</tt>.
     * 
     * @param items value to be assigned to property items
     */
    public void setItems(List<AssembleItem> items) {
        this.items = items;
    }

}
