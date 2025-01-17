package com.github.loadup.components.gateway.message.script.util;

/*-
 * #%L
 * loadup-components-gateway-core
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

import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 *
 */
@Component
public class ScriptSpringContextUtil implements ApplicationContextAware {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(ScriptSpringContextUtil.class);

    /**
     * Spring context
     */
    private static ApplicationContext applicationContext;

    /**
     * invoke a method of spring bean dynamically
     */
    public static Object invoke(String beanName, String method, Object... parmas) {
        try {
            Object obj = getBean(beanName);

            Class[] parameterTypes = new Class[parmas.length];
            for (int i = 0; i < parmas.length; i++) {
                parameterTypes[i] = parmas[i].getClass();
            }
            Method getMethod = obj.getClass().getMethod(method, parameterTypes);
            return getMethod.invoke(obj, parmas);
        } catch (Exception e) {
            LogUtil.error(logger, e, "ScriptSpringContextUtil invoke error,method:" + beanName + "." +
                    "method,params:" + parmas);
        }
        return null;
    }

    /**
     *
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * set applicationContext
     *
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ScriptSpringContextUtil.applicationContext = applicationContext;
    }

    /**
     * get bean
     *
     * @throws BeansException
     */
    public static Object getBean(String name) throws BeansException {
        return applicationContext.getBean(name);

    }

    /**
     * get All bean names
     */
    public static String[] getBeanDefinitionNames() {
        return applicationContext.getBeanDefinitionNames();
    }

    /**
     * get bean by classType
     *
     * @throws BeansException
     */
    public static <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(name, requiredType);
    }

    /**
     * is containsBean
     */
    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    /**
     * isSingleton
     *
     * @throws NoSuchBeanDefinitionException
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.isSingleton(name);
    }

    /**
     * get bean class type
     *
     * @throws NoSuchBeanDefinitionException
     */
    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.getType(name);
    }

    /**
     * get bean by alias
     *
     * @throws NoSuchBeanDefinitionException
     */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.getAliases(name);
    }

}
