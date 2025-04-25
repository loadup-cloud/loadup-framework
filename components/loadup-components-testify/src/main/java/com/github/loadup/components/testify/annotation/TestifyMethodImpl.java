
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


import com.github.loadup.components.testify.runtime.TestifyRuntimeContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 */
@Slf4j
public class TestifyMethodImpl implements TestifyMethod {

    protected Object instance;
    protected String group;
    private Method invoker;
    /* the invoke order */
    private int order = 0;

    public TestifyMethodImpl() {
    }

    public TestifyMethodImpl(Method method, Object instance) {
        this.invoker = method;
        this.instance = instance;
    }

    @Override
    public void invoke(TestifyRuntimeContext testifyRuntimeContext) {

        try {
            if (this.invoker.getParameterTypes().length == 0) {
                this.invoker.setAccessible(true);
                this.invoker.invoke(instance, new Object[]{});
                return;
            }

            if (this.invoker.getParameterTypes()[0].equals(TestifyRuntimeContext.class)) {
                this.invoker.setAccessible(true);
                this.invoker.invoke(instance, new Object[]{testifyRuntimeContext});
                return;
            }

        } catch (IllegalAccessException e) {
            log.info("出错了", e);
        } catch (IllegalArgumentException e) {
            log.info("出错了", e);
        } catch (InvocationTargetException e) {
            log.info("出错了", e);
        }
    }

    public Method getInvoker() {
        return invoker;
    }

    public void setInvoker(Method invoker) {
        this.invoker = invoker;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

}
