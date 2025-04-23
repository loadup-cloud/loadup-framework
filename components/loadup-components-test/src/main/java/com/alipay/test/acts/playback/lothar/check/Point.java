/**
 * Alipay.com Inc. Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.test.acts.playback.lothar.check;

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
 * 校验点
 *
 * @author qingqin
 * @version $Id: Point.java, v 0.1 2019年07月03日 上午12:10 qingqin Exp $
 */
public class Point {

    /**
     * 校验点描述
     *
     * mock 即校验，故 className#method 就是其概要信息
     */
    private String   className;

    private String   method;

    /** 该方法请求 mock 的次序*/
    private int      pointCounter;

    /** invoke id of mock data*/
    private String   mockSeq;

    /** 当前 invoke id*/
    private String   nowSeq;

    /** 是入参还是结果*/
    private boolean  input;

    /**
     * 如果是 结果，就放置在 0 号位置
     * 如果是 入参，按照顺序放置
     *
     */
    private Object[] paramsReal;

    private Object[] paramsMock;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getPointCounter() {
        return pointCounter;
    }

    public void setPointCounter(int pointCounter) {
        this.pointCounter = pointCounter;
    }

    public String getMockSeq() {
        return mockSeq;
    }

    public void setMockSeq(String mockSeq) {
        this.mockSeq = mockSeq;
    }

    public String getNowSeq() {
        return nowSeq;
    }

    public void setNowSeq(String nowSeq) {
        this.nowSeq = nowSeq;
    }

    public Object[] getParamsReal() {
        return paramsReal;
    }

    public void setParamsReal(Object[] paramsReal) {
        this.paramsReal = paramsReal;
    }

    public Object[] getParamsMock() {
        return paramsMock;
    }

    public void setParamsMock(Object[] paramsMock) {
        this.paramsMock = paramsMock;
    }

    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }
}
