/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.alipay.test.acts.utils;

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
import com.alipay.test.acts.model.VirtualComponent;
import com.alipay.test.acts.runtime.ActsRuntimeContext;
import com.alipay.test.acts.template.ActsTestBase;

/**
 * 
 * @author tianzhu.wtzh
 * @version $Id: ComponentsProcessor.java, v 0.1 2016年1月29日 下午4:43:26 tianzhu.wtzh Exp ${0xD}
 */
public class ComponentsProcessor {

    /**
     * 确定组件的执行环节
     * 
     * @param actsRuntimeContext
     */
    public static void getIndexForPreComp(ActsRuntimeContext actsRuntimeContext,
                                          ActsTestBase actsTest,
                                          VirtualComponent virtualComponent) {

        String cmdCmpExcuteIndex = virtualComponent.getNodeGroup();
        if (StringUtils.isEmpty(cmdCmpExcuteIndex)
            || StringUtils.equals(cmdCmpExcuteIndex, "Before数据准备")) {
            actsRuntimeContext.BeforePreparePreList.add(actsTest);
        } else if (StringUtils.equals(cmdCmpExcuteIndex, "After数据准备")) {
            actsRuntimeContext.AfterPreparePreList.add(actsTest);
        } else if (StringUtils.equals(cmdCmpExcuteIndex, "Before数据清理")) {
            actsRuntimeContext.BeforeClearPreList.add(actsTest);
        } else if (StringUtils.equals(cmdCmpExcuteIndex, "After数据清理")) {
            actsRuntimeContext.AfterClearPreList.add(actsTest);
        } else if (StringUtils.equals(cmdCmpExcuteIndex, "Before数据check")) {
            actsRuntimeContext.BeforeCheckPreList.add(actsTest);
        } else if (StringUtils.equals(cmdCmpExcuteIndex, "After数据check")) {
            actsRuntimeContext.AfterCheckPreList.add(actsTest);
        }
    }
}
