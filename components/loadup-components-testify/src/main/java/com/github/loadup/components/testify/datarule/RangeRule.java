/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.datarule;

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
 * 范围取值规则。
 *
 *
 *
 */
public class RangeRule implements RuleObject {

    /**
     * 最小边界值
     */
    private String minValue;

    /**
     * 最大边界值
     */
    private String maxValue;

    /**
     * 包含最小边界
     */
    private boolean containsMin;

    /**
     * 包含最大边界
     */
    private boolean containsMax;

    /**
     *
     *
     * @return property value of minValue
     */
    public String getMinValue() {
        return minValue;
    }

    /**
     *
     *
     * @param minValue value to be assigned to property minValue
     */
    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    /**
     *
     *
     * @return property value of maxValue
     */
    public String getMaxValue() {
        return maxValue;
    }

    /**
     *
     *
     * @param maxValue value to be assigned to property maxValue
     */
    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    /**
     *
     *
     * @return property value of containsMin
     */
    public boolean isContainsMin() {
        return containsMin;
    }

    /**
     *
     *
     * @param containsMin value to be assigned to property containsMin
     */
    public void setContainsMin(boolean containsMin) {
        this.containsMin = containsMin;
    }

    /**
     *
     *
     * @return property value of containsMax
     */
    public boolean isContainsMax() {
        return containsMax;
    }

    /**
     *
     *
     * @param containsMax value to be assigned to property containsMax
     */
    public void setContainsMax(boolean containsMax) {
        this.containsMax = containsMax;
    }
}
