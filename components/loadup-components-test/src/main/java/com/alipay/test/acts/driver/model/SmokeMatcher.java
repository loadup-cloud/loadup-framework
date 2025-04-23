package com.alipay.test.acts.driver.model;

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

import org.apache.commons.lang3.StringUtils;

/**
 * Smoke用例匹配器
 * 
 * @author tinghe
 */
public class SmokeMatcher {
    private String smokePrefixRegex = null;

    public SmokeMatcher() {
    }

    public SmokeMatcher(String smokePrefix) {
        this.smokePrefixRegex = smokePrefix;
    }

    /**
     * 匹配caseId，如果不需要进行smock则认为匹配成功，直接返回true
     * @param caseId
     * @return
     */
    public boolean match(String caseId) {
        if (!needSmoke()) {
            return true;
        }
        if (StringUtils.isBlank(caseId)) {
            return false;
        }
        return caseId.trim().matches(smokePrefixRegex + ".*");
    }

    /**
     * 是否需要进行smock，如果smock_test为空的，则不需要进行匹配
     */
    public boolean needSmoke() {
        return StringUtils.isNotBlank(smokePrefixRegex);
    }
}
