/**
 * Alipay.com Inc. Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.test.acts.enums;

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
 * @author pumo
 * @version $Id: YamlFieldEnum.java, v 0.1 2021年03月31日 11:07 PM pumo Exp $
 */
public enum YamlFieldEnum {

    ARGS("# Case Desc: 用例描述填这行\n\n# Arguments: List<Object>\n", "入参列表", 0, "[\n\n]\n"),
    FLAGS("# Flags: Map<String, Map<String, String>>\n", "比对标记列表", 1, "{\n\n}\n"),
    RESULT("# Result: Object\n", "结果对象", 2, "null\n"),
    EXCEPTION("# Exception: Object\n", "异常对象", 2, "null\n"),
    EVENTS("# Message Event: List<Map<String, Object>>\n", "消息事件列表", 3, "null\n"),
    PARAMS("# User-defined Params: Map<String, Object>\n", "自定义变量", 4, "{\nval1: \"sampleVal1\",\nval2: \"sampleVal2\"\n}\n"),
    MOCKS("# Virtual Mocks(Deprecated): List<VirtualMock>\n", "内置mock（已弃用）", 5, "null\n"),
    COMPOS("# Virtual Components: List<Map<String, Object>>\n", "组件化列表", 6, "[\n"
            + "  {\n"
            + "    useOrigData: true,\n"
            + "    caseId: null,\n"
            + "    componentClass: null\n"
            + "  }\n"
            + "]\n");

    /** code */
    private String code;

    /** description */
    private String desc;

    /** position in yaml */
    private int pos;

    /** default value */
    private String initVal;

    /**
     * constructor
     *
     * @param code
     * @param desc
     */
    private YamlFieldEnum(String code, String desc, int pos, String initVal) {
        this.code = code;
        this.desc = desc;
        this.pos = pos;
        this.initVal = initVal;
    }

    /**
     * Getter method for property <tt>code</tt>.
     *
     * @return property value of code
     */
    public String getCode() {
        return code;
    }

    /**
     * Getter method for property <tt>desc</tt>.
     *
     * @return property value of desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Getter method for property <tt>pos</tt>.
     *
     * @return property value of pos
     */
    public int getPos() {
        return pos;
    }

    /**
     * Getter method for property <tt>initVal</tt>.
     *
     * @return property value of initVal
     */
    public String getInitVal() {
        return initVal;
    }
}
