/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.utils;

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

import com.github.loadup.components.testify.model.VirtualComponent;
import com.github.loadup.components.testify.runtime.TestifyRuntimeContext;
import com.github.loadup.components.testify.template.TestifyTestBase;
import org.apache.commons.lang3.StringUtils;

/**
 *
 *
 */
public class ComponentsProcessor {

    /**
     * 确定组件的执行环节
     *
     * @param testifyRuntimeContext
     */
    public static void getIndexForPreComp(
            TestifyRuntimeContext testifyRuntimeContext,
            TestifyTestBase testifyTestBase,
            VirtualComponent virtualComponent) {

        String cmdCmpExcuteIndex = virtualComponent.getNodeGroup();
        if (StringUtils.isEmpty(cmdCmpExcuteIndex) || StringUtils.equals(cmdCmpExcuteIndex, "Before数据准备")) {
            testifyRuntimeContext.BeforePreparePreList.add(testifyTestBase);
        } else if (StringUtils.equals(cmdCmpExcuteIndex, "After数据准备")) {
            testifyRuntimeContext.AfterPreparePreList.add(testifyTestBase);
        } else if (StringUtils.equals(cmdCmpExcuteIndex, "Before数据清理")) {
            testifyRuntimeContext.BeforeClearPreList.add(testifyTestBase);
        } else if (StringUtils.equals(cmdCmpExcuteIndex, "After数据清理")) {
            testifyRuntimeContext.AfterClearPreList.add(testifyTestBase);
        } else if (StringUtils.equals(cmdCmpExcuteIndex, "Before数据check")) {
            testifyRuntimeContext.BeforeCheckPreList.add(testifyTestBase);
        } else if (StringUtils.equals(cmdCmpExcuteIndex, "After数据check")) {
            testifyRuntimeContext.AfterCheckPreList.add(testifyTestBase);
        }
    }
}
