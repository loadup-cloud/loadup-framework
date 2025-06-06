/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.annotation;

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

import com.github.loadup.components.testify.component.db.DBDatasProcessor;
import com.github.loadup.components.testify.component.handler.TestUnitHandler;
import com.github.loadup.components.testify.model.PrepareData;
import com.github.loadup.components.testify.runtime.TestifyRuntimeContext;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 工厂方法设计模式
 *
 *
 */
public class TestContextBuilder {

    /**
     * 根据context 构造对应的handler
     *
     * @param testifyRuntimeContext
     * @return
     */
    public TestUnitHandler buildContextHandler(TestifyRuntimeContext testifyRuntimeContext) {

        return new TestUnitHandler(testifyRuntimeContext);
    }

    /**
     * 根据测试数据及配置信息构造context信息
     *
     * @param caseId
     * @param prepareData
     * @param componentContext
     * @param dbDatasProcessor
     * @param testedMethod
     * @param testedObj
     * @return
     */
    public TestifyRuntimeContext buildTestContext(
            String caseId,
            PrepareData prepareData,
            Map<String, Object> componentContext,
            DBDatasProcessor dbDatasProcessor,
            Method testedMethod,
            Object testedObj) {

        return new TestifyRuntimeContext(
                caseId, prepareData, new HashMap<String, Object>(), dbDatasProcessor, testedMethod, testedObj);
    }
}
