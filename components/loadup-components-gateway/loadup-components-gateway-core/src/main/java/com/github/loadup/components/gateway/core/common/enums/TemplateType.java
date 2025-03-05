package com.github.loadup.components.gateway.core.common.enums;

/*-
 * #%L
 * loadup-components-gateway-core
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

import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public enum TemplateType {

    /**
     * parser
     */
    PARSER("PARSER", "parser template"),

    /**
     * assembler
     */
    ASSEMBLER("ASSEMBLER", "assembler template"),
    ;

    /**
     * code
     */
    private String code;

    /**
     * desc
     */
    private String desc;

    /**
     * 构造函数
     */
    TemplateType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据名字获取编码的枚举类型
     */
    public static TemplateType getByCode(String code) {
        for (TemplateType templateType : TemplateType.values()) {
            if (StringUtils.equalsIgnoreCase(templateType.getCode(), code)) {
                return templateType;
            }
        }
        return null;
    }

    /**
     * Getter method for property <tt>code</tt>.
     */
    public String getCode() {
        return code;
    }

    /**
     * Setter method for property <tt>code</tt>.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Getter method for property <tt>desc</tt>.
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Setter method for property <tt>desc</tt>.
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
