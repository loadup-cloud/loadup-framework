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

public class VirtualComponent extends TestNode {

    PrepareData prepareData;
    String componentId;
    String componentClass;
    boolean useOrigData;
    // 描述性组件表达式
    String compExpression;
    String compExcuteIndex;
    String componentDesc;

    public PrepareData getPrepareData() {
        return prepareData;
    }

    public void setPrepareData(PrepareData prepareData) {
        this.prepareData = prepareData;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentClass() {
        return componentClass;
    }

    public void setComponentClass(String componentClass) {
        this.componentClass = componentClass;
    }

    public String getCompExpression() {
        return compExpression;
    }

    public void setCompExpression(String compExpression) {
        this.compExpression = compExpression;
    }

    public String getCompExcuteIndex() {
        return compExcuteIndex;
    }

    public void setCompExcuteIndex(String compExcuteIndex) {
        this.compExcuteIndex = compExcuteIndex;
    }

    public String getComponentDesc() {
        return componentDesc;
    }

    public void setComponentDesc(String componentDesc) {
        this.componentDesc = componentDesc;
    }

    public boolean isUseOrigData() {
        return useOrigData;
    }

    public void setUseOrigData(boolean useOrigData) {
        this.useOrigData = useOrigData;
    }
}
