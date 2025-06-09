/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.component.enums;

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
 * 组件库类型。
 *
 *
 *
 */
public enum RepositoryType {

    /**
     * 用例准备库
     */
    PREPARE("ccprepare"),

    /**
     * 用例检查库
     */
    CHECK("cccheck"),

    /**
     * 用例清理库
     */
    CLEAR("ccclear"),

    /**
     * 用例mock库
     */
    MOCK("ccmock");

    private String value;

    private RepositoryType(String value) {
        this.value = value;
    }

    /**
     * 根据值获取枚举。
     *
     * @param value
     * @return
     */
    public static RepositoryType getRepositoryType(String value) {
        RepositoryType[] types = RepositoryType.values();
        for (RepositoryType type : types) {
            if (type.name().equals(value) || type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
