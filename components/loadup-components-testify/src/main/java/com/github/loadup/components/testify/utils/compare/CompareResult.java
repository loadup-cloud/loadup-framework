/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.utils.compare;

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

import java.util.HashSet;
import java.util.Set;
import org.springframework.util.CollectionUtils;

/**
 * 比较结果
 *
 * @author rong.zhang
 *
 */
public class CompareResult {

    /**
     * 是否一致
     */
    private Boolean same;

    /**
     * 所有比较不同点
     */
    private Set<CompareDetail> compareDetails;

    public CompareResult() {
        super();
    }

    public CompareResult(Boolean same) {
        super();
        this.same = same;
    }

    public Boolean getSame() {
        return same;
    }

    public void setSame(Boolean same) {
        this.same = same;
    }

    public Set<CompareDetail> getCompareDetails() {
        return compareDetails;
    }

    public void setCompareDetails(Set<CompareDetail> compareDetails) {
        this.compareDetails = compareDetails;
    }

    public void addCompareDetail(String key, Object srcObj, Object targetObj) {
        if (compareDetails == null) {
            compareDetails = new HashSet<CompareDetail>();
        }
        CompareDetail e = new CompareDetail();
        e.setKey(key);
        e.setExpect(srcObj == null ? null : srcObj.toString());
        e.setActual(targetObj == null ? null : targetObj.toString());
        compareDetails.add(e);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("compare result：" + (same ? "same" : "not same") + "\n");
        if (CollectionUtils.isEmpty(compareDetails)) {
            return sb.toString();
        }
        sb.append("compare detail:\n");
        for (CompareDetail compareDetail : compareDetails) {
            sb.append("--");
            sb.append("key=" + compareDetail.getKey() + "\n");
            sb.append("----expect=" + compareDetail.getExpect() + "\n");
            sb.append("----actual=" + compareDetail.getActual() + "\n");
        }
        return sb.toString();
    }
}
