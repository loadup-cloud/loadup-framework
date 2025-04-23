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

import java.util.LinkedList;

/**
 * 一个用例的校验报告，其包涵若干个校验点的差异（注意多个入参被拆分成多个差异点）
 *
 * 即一个 Point 可能会有多个 CheckPointDiff
 *
 * 为了方便序列化，放置一个默认构造器，这里就不和 lothar 保持一致了
 *
 * @author qingqin
 * @version $Id: CaseReport.java, v 0.1 2019年07月02日 下午5:57 qingqin Exp $
 */
public class CaseReport {

    public CaseReport() {

    }

    /**
     *
     * @param traceId 用例执行的真实 trace id
     */
    public CaseReport(String traceId) {
        this.nowTraceId = traceId;
    }

    /**
     * 当前 trace id
     */
    private String                     nowTraceId;

    /**
     * trace id of mock data
     */
    private String                     mockTraceId;

    /**
     * 按照方法调用时序、入参顺序排列
     */
    private LinkedList<CheckPointDiff> pointDiffList = new LinkedList<CheckPointDiff>();

    /**
     * 报告处理状态
     *
     */
    private Status                     status        = Status.INIT;

    /**
     * 校验点总次数，用例回放经过了多少次 mock
     */
    private int                        total         = 0;

    public enum Status {
        /**
         * 开始
         */
        INIT(0),

        /**
         * 进行中
         */
        PROCESSING(1),

        /**
         * 已完成
         */
        DONE(2);

        private int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }
    }

    public String getNowTraceId() {
        return nowTraceId;
    }

    public void setNowTraceId(String nowTraceId) {
        this.nowTraceId = nowTraceId;
    }

    public String getMockTraceId() {
        return mockTraceId;
    }

    public void setMockTraceId(String mockTraceId) {
        this.mockTraceId = mockTraceId;
    }

    public LinkedList<CheckPointDiff> getPointDiffList() {
        return pointDiffList;
    }

    public void setPointDiffList(LinkedList<CheckPointDiff> pointDiffList) {
        this.pointDiffList = pointDiffList == null ? new LinkedList<CheckPointDiff>()
            : pointDiffList;
    }

    public void addPointDiff(CheckPointDiff diff) {
        this.pointDiffList.add(diff);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

}
