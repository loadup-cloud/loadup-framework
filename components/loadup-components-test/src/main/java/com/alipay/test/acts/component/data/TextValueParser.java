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

import com.alibaba.fastjson2.JSON;
import com.alipay.test.acts.util.VelocityUtil;

/**
 * 文本对象解析器。
 * <p>
 * 支持多种类型文本转换，包含yaml、json等，同时支持自定义对象转换。
 * 
 * @author dasong.jds
 * @version $Id: TextValueParser.java, v 0.1 2015年6月9日 下午3:50:42 dasong.jds Exp $
 */
public class TextValueParser {

    public static final <T> T parse(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    public static final <T> T parse(String text, Class<T> clazz, Map<String, Object> context) {
        String ttext = VelocityUtil.evaluateString(context, text);
        return JSON.parseObject(ttext, clazz);
    }

    public static final Object parse(String text) {
        return JSON.parse(text);
    }

    public static final Object parse(String text, Map<String, Object> context) {
        String ttext = VelocityUtil.evaluateString(context, text);
        return parse(ttext);
    }

}
