/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.support;

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
 * 测试用例生成器。
 * 
 * @author dasong.jds
 * @version $Id: TestCaseFactory.java, v 0.1 2015年5月17日 下午5:42:31 dasong.jds Exp $
 */
public class TestCaseFactory {

    /**
     * 根据用例描述生成测试用例。
     * <p>
     * 暂时只是简单分号分割，比如：caseId;caseOption;caseDescription
     * 
     * @param caseExpr
     * @return
     */
    public static TestCase create(String caseExpr) {

        String[] exprs = caseExpr.split(";");

        String caseId = exprs[0];
        if (exprs.length == 2) {
            String desc = exprs[1];
            return create(caseId, desc);
        } else if (exprs.length >= 3) {
            String testOptionStr = exprs[1];
            String desc = exprs[2];
            return create(caseId, testOptionStr, desc);
        } else {
            return create(caseId, null);
        }
    }

    /**
     * 生成测试用例。
     * <p>
     * 可通过用例ID关联用例管理平台上用例描述。
     * 
     * @param caseId
     * @param caseDescription
     * @return
     */
    public static TestCase create(String caseId, String caseDescription) {

        return create(caseId, null, caseDescription);
    }

    /**
     * 创建测试用例。
     * 
     * @param caseId
     * @param options           选项，比如isRun:Y
     * @param caseDescription
     * @return
     */
    public static TestCase create(String caseId, String options, String caseDescription) {

        TestCase tc = new TestCase(caseId, parseOption(options), caseDescription);

        return tc;

    }

    /**
     * 选项分析。
     * 
     * @param options
     * @param testOption
     */
    private static CaseOption parseOption(String options) {

        if (StringUtils.isEmpty(options)) {
            return new CaseOption();
        }

        CaseOption caseOption = new CaseOption();

        String[] ops = options.split("\\|");
        String[] pair;
        for (String option : ops) {
            pair = option.split(":");
            if (pair.length == 2) {
                if ("isRun".equals(pair[0])) {
                    if ("N".equals(pair[1]) || "F".equals(pair[1]) || "false".equals(pair[1])) {
                        caseOption.setRun(false);
                    }
                }
            }
        }

        return caseOption;
    }
}
