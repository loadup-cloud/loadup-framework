/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.setter;

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
 * 设置对象flag的工具类
 *
 * @author tantian.wc
 *
 */
public class FlagSetter {

    /**
     * 当前类名
     */
    String clazzName;
    /**
     * 当前类中field对应的flag
     */
    Map<String, String> fieldFlags;

    /**
     * 可以使用getFlagSetter静态方法
     *
     * @param clazzName
     * @param fieldFlags
     */
    public FlagSetter(String clazzName, Map<String, String> fieldFlags) {
        super();
        this.clazzName = clazzName;
        this.fieldFlags = fieldFlags;
    }

    /**
     * 获取FlagSetter实例
     *
     * @param clazzName
     * @param fieldFlags
     * @return
     */
    public static FlagSetter getFlagSetter(String clazzName, Map<String, String> fieldFlags) {
        FlagSetter flagSetter = new FlagSetter(clazzName, fieldFlags);
        return flagSetter;
    }

    public FlagSetter set(String field, String value) {
        fieldFlags.put(field, value);
        return this;
    }

    public Map<String, String> getFieldFlags() {
        return fieldFlags;
    }

    public void setFieldFlags(Map<String, String> fieldFlags) {
        this.fieldFlags = fieldFlags;
    }

    public String getClazzName() {
        return clazzName;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }
}
