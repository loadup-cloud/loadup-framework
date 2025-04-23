/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.test.acts.playback.util;

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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Maps;

/**
 *
 * @author qingqin
 * @version $Id: ReflectUtil.java, v 0.1 2019年07月30日 下午10:15 qingqin Exp $
 */
public class ReflectUtil {

    private static final Logger                            LOGGER           = LoggerFactory
            .getLogger(ReflectUtil.class);

    private static ReflectUtil                             INSTANCE         = new ReflectUtil();

    private static Map<ClassLoader, Map<String, Class<?>>> loadedClassCache = Maps.newHashMap();

    private static Lock                                    classLoadLock    = new ReentrantLock();

    private static final String                            IGNORE_TYPE      = "Object";

    public static ReflectUtil getInstance() {
        return INSTANCE;
    }

    /**
     * 调用目标class目标方法
     *
     * @param instance   目标实例，对于static方法，instance为null
     * @param className  目标类，对于实例方法，className无用，对于static方法，用来loadClass
     * @param methodName 目标函数名
     * @param argTypes   目标入参类型，用String表示
     * @param args       目标入参
     *
     * @return 方法调用结果
     */
    public static Object invokeMethod(Object instance, String className, String methodName,
                                      String[] argTypes, Object[] args) {

        try {
            Class<?> clazz = null;
            Method method = null;
            if (instance != null) {
                // 实例方法
                clazz = instance.getClass();
                method = getMemberMethod(clazz, methodName, argTypes);
            } else {
                // static方法
                clazz = loadClassByCurrentThreadClassLoader(className);
                method = getMemberMethod(clazz, methodName, argTypes);
            }

            if (null == method) {
                //日志打印去掉，日志量太大了，比如说acctrans依赖的fc-common-lang-2.1.0.20160315.jar 包太老，没有getBizEventCode相关的方法
                return null;
            }

            method.setAccessible(true);
            return method.invoke(instance, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 调用目标class目标方法
     *
     * @param instance   目标实例，对于static方法，instance为null
     * @param clazz  目标类，对于实例方法，claszz无用，对于static方法，用来直接调用
     * @param methodName 目标函数名
     * @param argTypes   目标入参类型，用String表示
     * @param args       目标入参
     *
     * @return 方法调用结果
     */
    public static Object invokeMethod(Object instance, Class<?> clazz, String methodName,
                                      Class<?>[] argTypes, Object[] args) {

        try {
            Method method;
            if (instance != null) {
                // 实例方法
                clazz = instance.getClass();
                method = getMemberMethod(clazz, methodName, argTypes);
            } else {
                // static方法
                method = getMemberMethod(clazz, methodName, argTypes);
            }

            if (null == method) {
                //日志打印去掉，日志量太大了，比如说acctrans依赖的fc-common-lang-2.1.0.20160315.jar 包太老，没有getBizEventCode相关的方法
                return null;
            }

            method.setAccessible(true);
            return method.invoke(instance, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static Object invokeNoneArgMethod(Object instance, String methodName) {

        return invokeNoneArgMethod(instance, null, methodName);

    }

    public static Object invokeNoneArgMethod(Object instance, String className, String methodName) {

        return invokeMethod(instance, className, methodName, new String[0], new Object[0]);

    }

    public static Object invokeMethod(Object instance, String className, String methodName,
                                      List<String> argTypes, Object[] args) {

        return invokeMethod(instance, className, methodName, argTypes, args);
    }

    public static Object invokeMethod(Object instance, String className, String methodName,
                                      List<String> argTypes, List<Object> args) {

        return invokeMethod(instance, className, methodName,argTypes,
                ArrayUtils.toArray(args, new Object[0]));
    }

    /**
     * 传入classloader获取指定类clazz指定入参类型成员函数
     *
     * @param cl         目标classloader
     * @param clazzName  目标类名
     * @param methodName 目标函数名
     * @param argTypes   函数参数入参类型，这里用String表示
     * @return 目标函数
     */
    public static Method getMemberMethod(ClassLoader cl, String clazzName, String methodName,
                                         String[] argTypes) {
        try {
            Class<?> clazz = cl.loadClass(clazzName);
            return getMemberMethod(clazz, methodName, argTypes);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 获取指定类clazz（以及继承的）、指定方法名称、指定入参类型的private、protect、default、public的成员函数
     * class.getDeclaredMethods
     *
     * @param clazz      目标类
     * @param methodName 目标函数名
     * @param argTypes   函数参数入参类型，这里用String表示
     * @return 目标函数
     */
    public static Method getMemberMethod(Class<?> clazz, String methodName, String[] argTypes) {

        if (null == methodName) {
            return null;
        }

        Class<?> innerClazz = clazz;
        while (innerClazz != null && !StringUtils.equals(innerClazz.getSimpleName(), IGNORE_TYPE)) {
            try {

                Method[] methods = innerClazz.getDeclaredMethods();
                if (!ArrayUtils.isEmpty(methods)) {
                    for (Method each : methods) {
                        if (!methodName.equals(each.getName())) {
                            continue;
                        }
                        if (matchesParameterTypes(each.getParameterTypes(), argTypes)) {
                            each.setAccessible(true);
                            return each;
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.warn("getMemberField exception..." + innerClazz.getName() + ","
                        + ex.getMessage() + "," + ex.getClass().getName());
            }
            innerClazz = innerClazz.getSuperclass();
        }

        return null;
    }

    /**
     * 获取指定类clazz（以及继承的）、指定方法名称、指定入参类型的private、protect、default、public的成员函数
     * class.getDeclaredMethods
     *
     * @param clazz      目标类
     * @param methodName 目标函数名
     * @param argTypes   函数参数入参类型，这里用String表示
     * @return 目标函数
     */
    public static Method getMemberMethod(Class<?> clazz, String methodName, Class<?>[] argTypes) {

        if (null == methodName) {
            return null;
        }

        Class<?> innerClazz = clazz;
        while (innerClazz != null && !StringUtils.equals(innerClazz.getSimpleName(), IGNORE_TYPE)) {
            try {

                Method[] methods = innerClazz.getDeclaredMethods();
                if (!ArrayUtils.isEmpty(methods)) {
                    for (Method each : methods) {
                        if (methodName.equals(each.getName())) {
                            if (matchesParameterTypes(each.getParameterTypes(), argTypes)) {
                                each.setAccessible(true);
                                return each;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.warn("getMemberField exception..." + innerClazz.getName() + ","
                        + ex.getMessage() + "," + ex.getClass().getName());
            }
            innerClazz = innerClazz.getSuperclass();
        }

        return null;
    }

    /**
     * 传入目标classloader实例化目标类
     *
     * @param cl        目标classloader
     * @param clazzName 目标class名¶
     * @param argTypes  为空则获取无参构造函数
     * @param args      构造函数调用入参
     * @return 目标类实例
     */
    public static Object newInstance(ClassLoader cl, String clazzName, String[] argTypes,
                                     Object[] args) {

        Constructor<?> constructor = getConstructor(cl, clazzName, argTypes);
        if (null == constructor) {
            LOGGER.warn("Cannot found matches constructor!" + clazzName);
            return null;
        }
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            LOGGER.warn("constructor instance error!", e);
            return null;
        }
    }

    public static Object newInstance(String clazzName, String[] argTypes, Object[] args) {

        return newInstance(Thread.currentThread().getContextClassLoader(), clazzName, argTypes,
                args);
    }

    public static Object newInstance(String clazzName, List<String> argTypes, Object[] args) {

        return newInstance(Thread.currentThread().getContextClassLoader(), clazzName,
                argTypes.toArray(new String[0]) , args);
    }

    public static Object newInstance(String clazzName, List<String> argTypes, List<Object> args) {

        return newInstance(Thread.currentThread().getContextClassLoader(), clazzName,
                argTypes.toArray(new String[0]), ArrayUtils.toArray(args, new Object[0]));
    }

    /**
     * 传入classloader获取指定类clazz指定入参类型构造函数
     *
     * @param cl        目标classloader
     * @param clazzName 目标class名
     * @param argTypes  为空则获取无参构造函数
     * @return 目标构造函数
     */
    private static Constructor<?> getConstructor(ClassLoader cl, String clazzName, String[] argTypes) {
        try {

            Class<?> clazz = cl.loadClass(clazzName);
            Constructor<?>[] methods = clazz.getDeclaredConstructors();
            if (ArrayUtils.isEmpty(methods)) {
                return null;
            }
            for (Constructor<?> each : methods) {
                if (matchesParameterTypes(each.getParameterTypes(), argTypes)) {
                    each.setAccessible(true);
                    return each;
                }
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    public static Object getStaticField(ClassLoader targetCl, String className, String fieldName) {
        if (StringUtils.isBlank(className) || StringUtils.isBlank(fieldName)) {
            return null;
        }
        ClassLoader cl = targetCl;
        if (null == cl) {
            cl = Thread.currentThread().getContextClassLoader();
        }
        try {
            Class target = cl.loadClass(className);
            if (null == target) {
                LOGGER.warn("Current classloader" + cl + " hasn't " + className);
                return null;
            }
            Field field = getMemberField(target, fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            LOGGER.warn("getStaticField exception..." + e.getMessage());
            return null;
        }

    }

    /**
     * 获取某实例私有成员变量
     *
     * @param instance 目标实例
     * @param name     目标field名
     * @return 值
     */
    public static Object getObjectField(Object instance, String name) {
        if (null == instance) {
            return null;
        }
        Field field = getMemberField(instance.getClass(), name);
        if (null == field) {
            return null;
        }
        field.setAccessible(true);

        try {
            return field.get(instance);
        } catch (Exception e) {
            LOGGER.warn("getObjectField exception..." + e.getMessage());
            return null;
        }
    }

    /**
     * 给某实例私有成员变量赋值
     *
     * @param instance 目标实例
     * @param name     目标field名
     * @param value    目标field值
     */
    public static void setObjectField(Object instance, String name, Object value) {
        if (null == instance) {
            return;
        }
        Field field = getMemberField(instance.getClass(), name);
        if (null == field) {
            return;
        }
        field.setAccessible(true);

        try {
            field.set(instance, value);
        } catch (Exception e) {
            LOGGER.warn("getObjectField exception..." + e.getMessage());
        }
    }

    /**
     * 获取某实例私有成员属性
     *
     * @param clazz 实例
     * @param name     属性名
     * @return 目标属性
     */
    public static Field getMemberField(Class clazz, String name) {
        if (clazz == null) {
            return null;
        }
        Class<?> type = clazz;
        while (null != type && !StringUtils.equals(type.getSimpleName(), IGNORE_TYPE)) {
            try {
                Field f = type.getDeclaredField(name);
                if (f != null) {
                    return f;
                }
            } catch (Exception e) {
            }
            type = type.getSuperclass();
        }

        return null;
    }

    /**
     * 加载目标类，使用当前线程的类加载器，优先从内部缓存中获取，缓存中不存在才进行加载
     *
     * @param className
     * @return class
     */
    public static Class<?> loadClassByCurrentThreadClassLoader(String className) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // 先从缓存获取，缓存不存在才进行load，减少开销
        Map<String, Class<?>> clazzMap = loadedClassCache.get(classLoader);
        if (clazzMap != null && clazzMap.containsKey(className)) {
            return clazzMap.get(className);
        }

        classLoadLock.lock();
        try {
            // double check
            if (clazzMap != null && clazzMap.containsKey(className)) {
                return clazzMap.get(className);
            }

            Class<?> clazz = classLoader.loadClass(className);
            if (clazz != null) {
                Map<String, Class<?>> m = loadedClassCache.get(classLoader);
                if (m == null) {
                    m = Maps.newHashMap();
                    loadedClassCache.put(classLoader, m);
                }
                m.put(className, clazz);
                return clazz;
            }
        } catch (Exception e) {
            LoggerUtil.warn(LOGGER, "Load {0} error:{1}", className,
                    e.getMessage());
        } finally {
            classLoadLock.unlock();
        }

        return null;
    }

    public static boolean matchesParameterTypes(Class<?>[] methodParameterTypes, String[] argTypes) {
        if (ArrayUtils.isEmpty(argTypes)) {
            return ArrayUtils.isEmpty(methodParameterTypes);
        }

        if (ArrayUtils.isEmpty(methodParameterTypes)) {
            return false;
        }

        if (methodParameterTypes.length != argTypes.length) {
            return false;
        }

        for (int i = 0; i < argTypes.length; i++) {
            if (argTypes[i] == null) {
                return false;
            }
            if (!argTypes[i].equals(methodParameterTypes[i].getCanonicalName())) {
                return false;
            }
        }
        return true;

    }

    private static boolean matchesParameterTypes(Class<?>[] methodParameterTypes,
                                                 Class<?>[] argTypes) {
        if (ArrayUtils.isEmpty(argTypes)) {
            return ArrayUtils.isEmpty(methodParameterTypes);
        }

        if (ArrayUtils.isEmpty(methodParameterTypes)) {
            return false;
        }

        if (methodParameterTypes.length != argTypes.length) {
            return false;
        }

        for (int i = 0; i < argTypes.length; i++) {
            if (argTypes[i] == null) {
                return false;
            }
            if (!argTypes[i].getCanonicalName().equals(methodParameterTypes[i].getCanonicalName())) {
                return false;
            }
        }
        return true;

    }
}
