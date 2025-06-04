/* Copyright (C) LoadUp Cloud 2022-2025 */
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

import com.github.loadup.components.testify.log.TestifyLogUtil;
import com.github.loadup.components.testify.object.comparer.UnitComparer;
import com.github.loadup.components.testify.util.FileUtil;
import org.apache.commons.lang3.StringUtils;

public class FileComparer implements UnitComparer {

    @Override
    public boolean compare(Object expect, Object actual, String comparerFlagCode) {
        if (actual == null && StringUtils.isBlank(String.valueOf(expect))) {
            return true;
        }
        String expectStr = FileUtil.readFile(String.valueOf(expect));
        if (!StringUtils.equals(expectStr, String.valueOf(actual))) {
            TestifyLogUtil.addProcessLog("文件校验失败");
            TestifyLogUtil.addProcessLog("预期内容:" + expectStr);
            TestifyLogUtil.addProcessLog("实际内容:" + actual);
            return false;
        }
        return true;
    }
}
