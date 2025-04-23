package com.alipay.test.acts.utils.compare;

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
 * 比较结果详细
 * 
 * @author rong.zhang
 * @version $Id: CompareDetail.java, v 0.1 2015-10-8 上午11:16:53 rong.zhang Exp $
 */
public class CompareDetail {

    /**
     * 关键字
     */
    private String key;

    /**
     * 预期值
     */
    private String expect;

    /**
     * 实际值
     */
    private String actual;

    /**
     * Getter method for property <tt>key</tt>.
     * 
     * @return property value of key
     */
    public String getKey() {
        return key;
    }

    /**
     * Setter method for property <tt>key</tt>.
     * 
     * @param key value to be assigned to property key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Getter method for property <tt>expect</tt>.
     * 
     * @return property value of expect
     */
    public String getExpect() {
        return expect;
    }

    /**
     * Setter method for property <tt>expect</tt>.
     * 
     * @param expect value to be assigned to property expect
     */
    public void setExpect(String expect) {
        this.expect = expect;
    }

    /**
     * Getter method for property <tt>actual</tt>.
     * 
     * @return property value of actual
     */
    public String getActual() {
        return actual;
    }

    /**
     * Setter method for property <tt>actual</tt>.
     * 
     * @param actual value to be assigned to property actual
     */
    public void setActual(String actual) {
        this.actual = actual;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof CompareDetail) {
            if (key == null && ((CompareDetail) obj).getKey() == null) {
                return true;
            }
            if (key != null && ((CompareDetail) obj).getKey() == null) {
                return false;
            }
            if (key == null && ((CompareDetail) obj).getKey() != null) {
                return false;
            }

            return key.equals(((CompareDetail) obj).getKey());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
