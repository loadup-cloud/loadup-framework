package com.github.loadup.components.gateway.certification.model;

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
 * 编码枚举类
 */
public enum CharsetEnum {

    /**
     * utf-8 编码
     */
    UTF8("UTF-8"),

    /**
     * gbk编码
     */
    GBK("GBK"),

    /**
     * ISO-8859-1编码
     */
    ISO_8859_1("ISO-8859-1"),

    /**
     * GB2312
     */
    GB2312("GB2312"),
    ;

    /**
     * 编码
     */
    private String charSet;

    /**
     * 构造函数
     */
    private CharsetEnum(String charSet) {
        this.charSet = charSet;
    }

    /**
     * 根据String获取对应编码枚举
     */
    public static CharsetEnum getByName(String input) {
        for (CharsetEnum charsetEnum : CharsetEnum.values()) {
            if (StringUtils.equals(input, charsetEnum.getCharSet())) {
                return charsetEnum;
            }
        }
        return null;
    }

    /**
     * 
     */
    public String getCharSet() {
        return charSet;
    }

    /**
     * 
     */
    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }
}
