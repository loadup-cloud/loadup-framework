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
 * 测试数据，每个case对应一个
 *
 * @author tantian.wc
 * @version $Id: PrepareData.java, v 0.1 2015年10月13日 上午10:17:03 tantian.wc Exp $
 */
public class PrepareData {

    //description
    private String description;
    //入参
    private VirtualArgs args;
    //前置的db数据
    private VirtualDataSet depDataSet;
    //预期的db数据
    private VirtualDataSet expectDataSet;
    //预期结果
    private VirtualResult expectResult;
    //预期事件
    private VirtualEventSet expectEventSet;
    //异常预期
    private VirtualException expectException;
    //mock信息
//    private VirtualMockSet      virtualMockSet;
    //自定义参数
    private VirtualParams virtualParams;
    //组件
    private VirtualComponentSet virtualComponentSet = new VirtualComponentSet();

    /**
     * default constructor
     * set all object members to not null
     */
    public PrepareData() {
        this(new VirtualArgs(), new VirtualDataSet(), new VirtualDataSet(), new VirtualResult(),
                new VirtualEventSet(), new VirtualException(),
                new VirtualParams());
    }

    private PrepareData(VirtualArgs virtualArgs, VirtualDataSet virtualDataSet,
                        VirtualDataSet virtualDataSet2, VirtualResult virtualResult,
                        VirtualEventSet virtualEventSet, VirtualException virtualException,
                        VirtualParams virtualParams) {
        this.args = virtualArgs;
        this.depDataSet = virtualDataSet;
        this.expectDataSet = virtualDataSet2;
        this.expectResult = virtualResult;
        this.expectEventSet = virtualEventSet;
        this.expectException = virtualException;
        this.virtualParams = virtualParams;
    }

    /**
     * Getter method for property <tt>description</tt>.
     *
     * @return property value of description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter method for property <tt>description</tt>.
     *
     * @param description value to be assigned to property description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public VirtualArgs getArgs() {
        return args;
    }

    public void setArgs(VirtualArgs args) {
        this.args = args;
    }

    public VirtualDataSet getDepDataSet() {
        return depDataSet;
    }

    public void setDepDataSet(VirtualDataSet depDataSet) {
        this.depDataSet = depDataSet;
    }

    public VirtualDataSet getExpectDataSet() {
        return expectDataSet;
    }

    public void setExpectDataSet(VirtualDataSet expectDataSet) {
        this.expectDataSet = expectDataSet;
    }

    public VirtualResult getExpectResult() {
        return expectResult;
    }

    public void setExpectResult(VirtualResult expectResult) {
        this.expectResult = expectResult;
    }


    public VirtualException getExpectException() {
        return expectException;
    }

    public void setExpectException(VirtualException expectException) {
        this.expectException = expectException;
    }

    public VirtualEventSet getExpectEventSet() {
        return expectEventSet;
    }

    public void setExpectEventSet(VirtualEventSet expectEventSet) {
        this.expectEventSet = expectEventSet;
    }

    public VirtualParams getVirtualParams() {
        return virtualParams;
    }

    public void setVirtualParams(VirtualParams virtualParams) {
        this.virtualParams = virtualParams;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "PrepareData [description=" + description + "]";
    }

    public VirtualComponentSet getVirtualComponentSet() {
        return virtualComponentSet;
    }

    public void setVirtualComponentSet(VirtualComponentSet virtualComponentSet) {
        this.virtualComponentSet = virtualComponentSet;
    }

}
