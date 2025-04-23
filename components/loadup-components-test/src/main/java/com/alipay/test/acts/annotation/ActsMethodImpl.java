/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.alipay.test.acts.annotation;

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


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.test.acts.runtime.ActsRuntimeContext;

/**
 * 
 * @author tianzhu.wtzh
 * @version $Id: ActsMethodImpl.java, v 0.1 2016年5月12日 上午11:25:31 tianzhu.wtzh Exp $
 */
public class ActsMethodImpl implements IActsMethod {

    private static final Logger logger = LoggerFactory.getLogger(ActsMethodImpl.class);

    private Method              invoker;

    protected Object            instance;

    protected String            group;

    /* the invoke order */
    private int                 order  = 0;

    public ActsMethodImpl() {
    }

    public ActsMethodImpl(Method method, Object instance) {
        this.invoker = method;
        this.instance = instance;
    }

    @Override
    public void invoke(ActsRuntimeContext actsRuntimeContext) {

        try {
            if (this.invoker.getParameterTypes().length == 0) {
                this.invoker.setAccessible(true);
                this.invoker.invoke(instance, new Object[] {});
                return;
            }

            if (this.invoker.getParameterTypes()[0].equals(ActsRuntimeContext.class)) {
                this.invoker.setAccessible(true);
                this.invoker.invoke(instance, new Object[] { actsRuntimeContext });
                return;
            }

        } catch (IllegalAccessException e) {
            logger.info("出错了", e);
        } catch (IllegalArgumentException e) {
            logger.info("出错了", e);
        } catch (InvocationTargetException e) {
            logger.info("出错了", e);
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
