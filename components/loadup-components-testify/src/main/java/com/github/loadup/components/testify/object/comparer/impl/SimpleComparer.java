package com.github.loadup.components.testify.object.comparer.impl;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import com.github.loadup.components.testify.constant.TestifyYamlConstants;
import com.github.loadup.components.testify.object.comparer.UnitComparer;

/**
 * "Y" flag 校验器
 *
 * @author midang
 *
 */
public class SimpleComparer implements UnitComparer {

    private final static String lineSeparator = File.separator;

    public static List<String> ignoreList = null;

    /**
     * 按字符串比较
     *
     * <pre>
     * compare("null",null)     = true
     * compare("null","null")   = true
     * compare("abc","abc")     = true
     * </pre>
     *
     * @param expect
     * @param actual
     * @return
     */
    @Override
    public boolean compare(Object expect, Object actual, String comparerFlagCode) {
        if (ignoreList == null) {
            ignoreList = new ArrayList<String>();
            String ignoreStr = "";// TestUtils.getSofaConfig(ActsConfigConstants.IGNORE_STRING_LIST_KEY);
            if (StringUtils.isNotBlank(ignoreStr)) {
                for (String ignoreString : ignoreStr.split(",")) {
                    ignoreList.add(ignoreString);
                }
            }
        }

        String exp = String.valueOf(expect);
        if (actual == null && (StringUtils.isBlank(exp) || "null".equals(exp))) {
            return true;
        }
        if (exp != null) {
            exp = exp.replace("\"\"", "\"");
            exp = exp.replace(TestifyYamlConstants.LINESEPARATOR, lineSeparator);
        }

        //仅在两者均不为空时开始过滤指定字符集
        if (ignoreList.size() > 0 && exp != null && actual != null) {
            for (String ignoreString : ignoreList) {
                exp = exp.replaceAll(ignoreString, "");
                actual = String.valueOf(actual).replaceAll(ignoreString, "");
            }
        }

        return StringUtils.equals(exp, String.valueOf(actual));
    }

}
