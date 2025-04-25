/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.data;

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
 * 原子字段类型。
 *
 * 
 *
 */
public class MetaFieldType {

    /**
     * 类型描述
     */
    private final String typeDesc;

    /**
     * @param typeDesc
     */
    public MetaFieldType(String typeDesc) {
        super();
        int index = typeDesc.lastIndexOf(".");
        if (index > 0) {
            this.typeDesc = typeDesc.substring(index + 1);
        } else {
            this.typeDesc = typeDesc;
        }
    }

    /**
     * @param type
     */
    public MetaFieldType(Class<?> type) {
        super();
        this.typeDesc = type.getSimpleName();
    }

    /**
     *
     *
     * @return property value of typeDesc
     */
    public String getTypeDesc() {
        return typeDesc;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return typeDesc;
    }

}
