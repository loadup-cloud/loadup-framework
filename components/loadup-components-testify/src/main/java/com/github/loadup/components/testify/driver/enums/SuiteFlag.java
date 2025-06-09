/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.driver.enums;

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
 * 用例标签枚举
 *
 *
 *
 */
public enum SuiteFlag {
    TESTCASE("TC", "TestCase", "正常执行用例", "正常执行用例"),

    TESTSUITE("TS", "TestSuite", "正常执行测试套件", "正常执行测试套件"),

    DELETE("DEL", "SkipTest", "跳过执行", "跳过执行");

    /**
     * 编码
     */
    private final String code;

    /**
     * 英文名
     */
    private final String englishName;

    /**
     * 中文名
     */
    private final String chineseName;

    /**
     * 描述
     */
    private final String description;

    private SuiteFlag(String code, String englishName, String chineseName, String description) {
        this.code = code;
        this.englishName = englishName;
        this.chineseName = chineseName;
        this.description = description;
    }

    /**
     * 通过状态码获取ENUM的名字
     *
     * @param statusCode
     * @return
     */
    public static SuiteFlag getByCode(String code) {
        for (SuiteFlag p : SuiteFlag.values()) {
            if (p.getCode().equalsIgnoreCase(code)) {
                return p;
            }
        }

        return null;
    }

    /**
     * 根据英文名称查询枚举。
     *
     * @param englishName 英文名称。
     * @return 枚举。
     */
    public static SuiteFlag getByEnglishName(String englishName) {
        for (SuiteFlag value : SuiteFlag.values()) {
            if (StringUtils.equals(englishName, value.getEnglishName())) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据中文名称查询枚举。
     *
     * @param chineseName 中文名称。
     * @return 枚举。
     */
    public static SuiteFlag getByChineseName(String chineseName) {
        for (SuiteFlag value : SuiteFlag.values()) {
            if (StringUtils.equals(chineseName, value.getChineseName())) {
                return value;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getChineseName() {
        return chineseName;
    }

    public String getDescription() {
        return description;
    }
}
