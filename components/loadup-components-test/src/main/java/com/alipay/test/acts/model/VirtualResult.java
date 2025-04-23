package com.alipay.test.acts.model;

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
 * 期望结果
 * @author tantian.wc
 * @version $Id: VirtualResult.java, v 0.1 2015年10月13日 上午10:29:05 tantian.wc Exp $
 */
public class VirtualResult extends TestUnit {

    /** 结果对象 */
    private VirtualObject result;

    public VirtualResult() {
        this.result = new VirtualObject();
    }

    public VirtualObject getVirtualObject() {
        return result;
    }

    public VirtualResult(Object resultObj) {

        this.result = new VirtualObject(resultObj);

    }

    public String getResultClazz() {
        if (result == null) {
            return null;
        }
        return result.getObjClass();
    }

    public void setResultClazz(String resultClazz) {
        if (this.result == null) {
            this.result = new VirtualObject();

        }
        result.setObjClass(resultClazz);
    }

    public Object getResultObj() {
        if (result == null) {
            return null;
        }
        return result.getObject();
    }

    public VirtualObject getResult() {
        return result;
    }

    public void setResult(VirtualObject result) {
        this.result = result;
    }

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "VirtualResult [result=" + result + "]";
    }

}
