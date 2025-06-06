/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.data.impl;

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

import com.github.loadup.components.testify.data.RuleDataStore;
import com.github.loadup.components.testify.manage.TestLogger;
import java.util.Properties;

/**
 * 文件类规则数据存储。
 *
 *
 *
 */
public class FileRuleDataStore implements RuleDataStore {

    /**
     * 数据
     */
    private Properties properties;

    /**
     * @see RuleDataStore#getRule(String, String)
     */
    @Override
    public String getRule(String system, String name) {

        Properties props = properties;

        if (props == null) {
            props = new Properties();
            try {
                props.load(this.getClass().getClassLoader().getResourceAsStream("ruledata/rule_data.properties"));
            } catch (Exception e) {
                TestLogger.getLogger().warn("can't load file: ruledata/rule_data.properties");
            }
            properties = props;
        }

        return props.getProperty(name);
    }

    /**
     * @see RuleDataStore#getMappedRule(String, String)
     */
    @Override
    public String getMappedRule(String system, String name) {
        // 暂不处理映射关系
        return null;
    }
}
