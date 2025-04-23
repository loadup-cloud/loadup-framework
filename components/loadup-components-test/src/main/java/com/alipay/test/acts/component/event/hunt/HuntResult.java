/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.event.hunt;

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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 
 * @author jie.peng
 * @version $Id: HuntResult.java, v 0.1 2015-6-29 下午06:29:26 jie.peng Exp $
 */
public class HuntResult {

    /**事件内容，对于本地模式抓取结果为具体对象，对于远程模式抓取的结果为一段日志*/
    private Object              eventContext;

    /**
     * 事件实际属性值，key为对应的事件属性名称，value为属性对应的实际值
     */
    private Map<String, String> actual;

    public Object getEventContext() {
        return eventContext;
    }

    public void setEventContext(Object eventContext) {
        this.eventContext = eventContext;
    }

    public Map<String, String> getActual() {
        return actual;
    }

    public void setActual(Map<String, String> actual) {
        this.actual = actual;
    }

    public void addActual(String key, String value) {
        if (null == actual) {
            this.actual = new HashMap<String, String>();
        }
        this.actual.put(key, value);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }

}
