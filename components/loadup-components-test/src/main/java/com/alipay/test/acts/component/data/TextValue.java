/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.data;

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

import java.util.Map;

/**
 * 对象文本。
 * <p>
 * 文本的形式表示对象。
 * 
 * @author dasong.jds
 * @version $Id: TextValue.java, v 0.1 2015年6月9日 下午3:57:58 dasong.jds Exp $
 */
public class TextValue {

    /** 对象文本表示 */
    private final String text;

    /**
     * @param text
     */
    public TextValue(String text) {
        super();
        this.text = text;
    }

    /**
     * 转换为特定类型对象。
     * 
     * @param clazz
     * @return
     */
    public <T> T toObject(Class<T> clazz) {

        return TextValueParser.parse(text, clazz);
    }

    public <T> T toObject(Class<T> clazz, Map<String, Object> context) {
        return TextValueParser.parse(text, clazz, context);
    }

    public Object toObject() {
        return TextValueParser.parse(text);
    }

    public Object toObject(Map<String, Object> context) {
        return TextValueParser.parse(text, context);
    }

    /**
     * Getter method for property <tt>text</tt>.
     * 
     * @return property value of text
     */
    public String getText() {
        return text;
    }

}
