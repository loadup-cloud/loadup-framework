/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.enums;

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

/**
 * 用于标记yaml不同区域的标题
 *
 *
 *
 */
public enum YamlFieldEnum {
    DESCRIPTION("description", "case描述"),
    ARGS("args", "入参列表"),
    FLAGS("flags", "比对标记列表"),
    RESULT("result", "结果对象"),
    EXCEPTION("exception", "异常"),
    PARAMS("params", "自定义变量"),
    COMPOS("components", "组件化列表");

    /**
     * code
     */
    private String code;

    /**
     * description
     */
    private String desc;

    /**
     * constructor
     *
     * @param code
     * @param desc
     */
    private YamlFieldEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     *
     *
     * @return property value of code
     */
    public String getCode() {
        return code;
    }

    /**
     *
     *
     * @return property value of desc
     */
    public String getDesc() {
        return desc;
    }
}
