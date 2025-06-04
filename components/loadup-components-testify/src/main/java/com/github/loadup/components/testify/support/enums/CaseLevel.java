/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.support.enums;

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
 * 用例等级。
 *
 *
 *
 */
public enum CaseLevel {
    HIGH("H", "high"),

    MEDIUM("M", "medium"),

    LOW("L", "low");

    /**
     * 简码
     */
    private final String code;

    /**
     * 别名
     */
    private final String alias;

    private CaseLevel(String code, String alias) {
        this.alias = alias;
        this.code = code;
    }

    /**
     * 根据简码获取枚举。
     *
     * @param code
     * @return
     */
    public static CaseLevel getLevelByCode(String code) {
        for (CaseLevel l : values()) {
            if (l.code.equals(code)) {
                return l;
            }
        }
        return null;
    }

    /**
     *
     *
     * @return property value of alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     *
     *
     * @return property value of code
     */
    public String getCode() {
        return code;
    }
}
