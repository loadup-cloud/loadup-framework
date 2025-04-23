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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 牛逼的代码不需要注释
 * @author xiuzhu.hp
 */
public class IVTableGroupCmdMethodImpl implements IVTableGroupCmdMethod {

    private static final Logger logger = LoggerFactory.getLogger(ActsMethodImpl.class);

    private Object instance;

    private Method targetMethod;

    public IVTableGroupCmdMethodImpl() {
    }

    public IVTableGroupCmdMethodImpl(Object instance, Method targetMethod) {
        this.instance = instance;
        this.targetMethod = targetMethod;
    }

    @Override
    public void invoke(String tableName, String groupId) {

        try {
            if (this.targetMethod.getParameterTypes().length == 0) {
                this.targetMethod.setAccessible(true);
                this.targetMethod.invoke(instance, new Object[] {});
                return;
            }

            if (this.targetMethod.getParameterTypes().length == 1
                    && this.targetMethod.getParameterTypes()[0].equals(String.class)) {
                this.targetMethod.setAccessible(true);
                this.targetMethod.invoke(instance, new Object[] { tableName });
                return;
            }

            if (this.targetMethod.getParameterTypes().length == 2
                && this.targetMethod.getParameterTypes()[0].equals(String.class)
                && this.targetMethod.getParameterTypes()[1].equals(String.class)) {
                this.targetMethod.setAccessible(true);
                this.targetMethod.invoke(instance, new Object[] { tableName, groupId });
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

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

}
