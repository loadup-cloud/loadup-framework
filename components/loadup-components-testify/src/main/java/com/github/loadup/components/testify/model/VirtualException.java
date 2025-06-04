/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.model;

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
 * 异常期望值
 *
 * @author tantian.wc
 *
 */
public class VirtualException extends TestUnit {

    /**
     * 异常对象
     */
    private VirtualObject expectException;

    public VirtualException() {
        this.expectException = new VirtualObject();
    }

    public VirtualException(Object expectException) {
        this.expectException = new VirtualObject(expectException);
    }

    public VirtualObject getVirtualObject() {

        return expectException;
    }

    public String getExceptionClass() {
        if (expectException == null) {
            return null;
        }
        return expectException.getObjClass();
    }

    @Deprecated
    public void setExceptionClass(String exceptionClass) {
        // 该方法已废弃，为了兼容暂时保留；请不要添加方法实现，否则会导致yaml dump异常
    }

    public Object getExpectExceptionObject() {
        if (expectException == null) {
            return null;
        }
        return expectException.getObject();
    }

    @Deprecated
    public void setExpectExceptionObject(Object expectException) {
        // 该方法已废弃，为了兼容暂时保留；请不要添加方法实现，否则会导致yaml dump异常
    }

    public VirtualObject getExpectException() {

        return expectException;
    }

    public void setExpectException(VirtualObject virtualObject) {
        this.expectException = virtualObject;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "VirtualException [expectException=" + expectException + "]";
    }
}
