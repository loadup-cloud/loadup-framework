/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.util;

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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.ContextClassLoaderLocal;

/**
 * bean工具类
 *
 * @author midang
 *
 */
public class BeanUtilsBean {

    /**
     * Contains <code>BeanUtilsBean</code> instances indexed by context classloader.
     */
    private static final ContextClassLoaderLocal beansByClassLoader = new ContextClassLoaderLocal() {
        // Creates the default instance used when the context classloader is unavailable
        @Override
        protected Object initialValue() {
            return new BeanUtilsBean();
        }
    };

    /**
     * Gets the instance which provides the functionality for {@link BeanUtils}.
     * This is a pseudo-singleton - an single instance is provided per (thread) context classloader.
     * This mechanism provides isolation for web apps deployed in the same container.
     */
    public static synchronized BeanUtilsBean getInstance() {
        return (BeanUtilsBean) beansByClassLoader.get();
    }

    /**
     * Sets the instance which provides the functionality for {@link BeanUtils}.
     * This is a pseudo-singleton - an single instance is provided per (thread) context classloader.
     * This mechanism provides isolation for web apps deployed in the same container.
     */
    public static synchronized void setInstance(BeanUtilsBean newInstance) {
        beansByClassLoader.set(newInstance);
    }

    /**
     * setProperty
     *
     * @param bean
     * @param name
     * @param value
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public void setProperty(Object bean, String name, Object value)
            throws SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = getField(bean.getClass(), name);
        field.setAccessible(true);
        field.set(bean, value);
    }

    /**
     * getPropertyValue
     *
     * @param bean
     * @param name
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    public Object getProperty(Object bean, String name)
            throws IllegalAccessException, InvocationTargetException, SecurityException, NoSuchFieldException {
        Field field = getField(bean.getClass(), name);
        field.setAccessible(true);
        return field.get(bean);
    }

    /**
     * getField
     *
     * @param clazz
     * @param name
     * @return
     * @throws SecurityException
     */
    @SuppressWarnings({"rawtypes"})
    private Field getField(Class clazz, String name) throws SecurityException {
        if (clazz == null || clazz == Object.class) {
            return null;
        }

        Field field = null;
        try {
            field = clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            Class superClazz = clazz.getSuperclass();
            field = getField(superClazz, name);
        }
        return field;
    }
}
