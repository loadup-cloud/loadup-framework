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

import com.github.loadup.components.testify.annotation.testify.*;
import com.github.loadup.components.testify.component.handler.TestUnitHandler;
import com.github.loadup.components.testify.runtime.TestifyRuntimeContext;
import com.github.loadup.components.testify.template.TestifyTestBase;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;

/**
 * executor method, do execute with prepare、execute、check and clean
 * if you has any problem, please contact with xiuzhu.hp in ali-wang-wang
 *
 * @author hupeng
 */
@Slf4j
public class TestifyExecutorMethod extends TestifyMethodImpl {

    /**
     * constructor
     *
     * @param method
     * @param instance
     * @param exeAnnotation
     */
    public TestifyExecutorMethod(Method method, Object instance, Executor executor) {
        super(method, instance);
        this.group = executor.group();
        this.setOrder(executor.Order());
    }

    @Override
    public void invoke(TestifyRuntimeContext testifyRuntimeContext) {
        log.info("=============================[" + this.group + " begin]=============================\r\n");

        /* conversion */
        TestifyTestBase testbase = (TestifyTestBase) this.instance;

        TestUnitHandler testUnitHandler = testbase.getTestUnitHandler();

        try {

            log.info(
                    "=============================[" + this.group + " prepare begin]=============================\r\n");

            /* do prepare */
            try {

                testbase.invokeTestifyMethods(BeforePrepare.class, testifyRuntimeContext);
                testUnitHandler.clearDepData(null, this.group);
                testUnitHandler.prepareDepData(null, this.group);

            } finally {
                testbase.invokeTestifyMethods(AfterPrepare.class, testifyRuntimeContext);
            }

            log.info("=============================[" + this.group + " prepare end]=============================\r\n");

            log.info("=============================[" + this.group + " invoke begin]=============================\r\n");

            /* do the method with @Executor on it */
            super.invoke(testifyRuntimeContext);

            log.info("=============================[" + this.group + " invoke end]=============================\r\n");

            log.info("=============================[" + this.group + " check begin]=============================\r\n");

            /* do check */
            testbase.invokeTestifyMethods(BeforeCheck.class, testifyRuntimeContext);

            try {
                testUnitHandler.checkExpectDbData(null, this.group);
            } finally {
                testbase.invokeTestifyMethods(AfterCheck.class, testifyRuntimeContext);
            }

            log.info("=============================[" + this.group + " check end]=============================\r\n");

        } finally {
            log.info("=============================[" + this.group + " clean begin]=============================\r\n");

            /* do clean */
            try {
                testbase.invokeTestifyMethods(BeforeClean.class, testifyRuntimeContext);
                testUnitHandler.clearDepData(null, this.group);
                testUnitHandler.clearExpectDBData(null, this.group);
            } finally {

                try {
                    testbase.invokeTestifyMethods(AfterClean.class, testifyRuntimeContext);
                } finally {
                    log.info("=====================[" + this.group + " clean end]======================\r\n");
                }
            }
        }
        log.info("=====================[" + this.group + " end]======================\r\n");
    }
}
