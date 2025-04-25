/**

 * Copyright (c) 2004-2011 All Rights Reserved.
 */
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.aop.framework.AopProxyUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson2.JSONObject;

/**
 * Object工具处理类
 *
 * @author jie.peng
 *
 */
@SuppressWarnings("unchecked")
public class ObjectUtil {

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(ObjectUtil.class);

    /**
     * 从字符串中解析出目标对象
     *
     * @param source 待解析的源字符串，格式为：billKeyName=测试数据1|status=O</br>
     * @param obj    期望解析的目标对象,要求obj必须要有简单的set方法，入参为String,且只有一个入参
     * @return
     */
    public static Object restoreObject(String source, Object obj) {

        if (StringUtils.isBlank(source)) {
            return obj;
        }
        String[] keyValues = StringUtils.split(source, "|");
        Class clazz = obj.getClass();
        for (String keyValue : keyValues) {

            String[] detail = keyValue.split("=", 2);
            String methodName = "set" + StringUtils.capitalize(detail[0].trim());

            Method method;
            try {
                method = clazz.getMethod(methodName, String.class);
                method.setAccessible(true);
                method.invoke(obj, detail[1]);
            } catch (Exception e) {
                logger.error("解析领域对象时出现异常,source=" + source, e);
            }
        }
        return obj;
    }

    /**
     * @param methodName
     * @param testedObj
     * @return
     */
    public static Method findMethod(String methodName, Object testedObj) {

        Class<?>[] clazzes = AopProxyUtils.proxiedUserInterfaces(testedObj);

        for (Class<?> clazz : clazzes) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (StringUtils.equals(method.getName(), methodName)) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * 从字符串中解析出目标对象
     *
     * @param source 待解析的源字符串，格式为：billKeyName=测试数据1|status=O</br>
     * @param obj    期望解析的目标对象,要求obj必须要有简单的set方法,且只有一个入参
     * @return
     */
    public static Object load(String source, Object obj) {

        if (StringUtils.isBlank(source)) {
            return obj;
        }
        String[] keyValues = StringUtils.split(source, "|");
        for (String keyValue : keyValues) {
            String[] detail = keyValue.split("=", 2);
            setValue(obj, detail[0].trim(), detail[1]);
        }
        return obj;
    }

    /**
     * 为指定对象obj的指定属性propertyName写入指定数值value
     *
     * @param obj
     * @param propertyName
     * @param value
     */
    public static void setValue(Object obj, String propertyName, String value) {
        if (null == obj) {
            return;
        }
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (!StringUtils.equals(propertyName, field.getName()) || field.getClass().isPrimitive()) {
                continue;
            }
            field.setAccessible(true);
            try {
                Class propertyClazz = field.getClass();
                if (propertyClazz.isInstance(new String())) {
                    field.set(obj, value);
                } else if (propertyClazz.isInstance(Boolean.TRUE)) {
                    field.set(obj, Boolean.valueOf(value));
                } else if (propertyClazz.isEnum()) {
//                    field.set(obj, EnumUtil.getEnumByName(propertyClazz, value));
//                } else if (propertyClazz.isInstance(new Integer(1))) {
//                    field.set(obj, new Integer(value));
                } else if ("int".equals(propertyClazz.getName())) {
                    field.set(obj, Integer.parseInt(value));
                } else if (propertyClazz.isInstance(new HashSet<String>())) {
                    Set<String> propertyvalue = new HashSet<String>();
                    for (int i = 0; i < value.length(); i++) {
                        propertyvalue.add(value + String.valueOf(i));
                    }
                    field.set(obj, propertyvalue);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从字符串描述解析出列表
     *
     * @param source
     * @return
     */
    public static List<String> resolveList(String source) {
        List<String> list = new ArrayList<String>();
        if (StringUtils.isBlank(source)) {
            return list;
        }
        String[] elements = StringUtils.split(source, "|");
        for (String each : elements) {
            list.add(each);
        }
        return list;
    }

    /**
     * 从字符串描述解析出列表，分隔符可以自定义
     *
     * @param source
     * @param separater
     * @return
     */
    public static List<String> resolveList(String source, String separater) {
        List<String> list = new ArrayList<String>();
        if (StringUtils.isBlank(source)) {
            return list;
        }
        String[] elements = StringUtils.split(source, separater);
        for (String each : elements) {
            list.add(each);
        }
        return list;
    }

    /**
     * 从指定对象实例复制其对应的属性至map，返回map的key值为其对应的属性名
     *
     * @param obj
     * @param params
     * @return
     */

    public static Map<String, String> copyProperties(Object obj, String... params) {
        Map<String, String> result = new HashMap<String, String>();
        if (null == obj || null == params || params.length == 0) {
            return result;
        }

        Class clazz = obj.getClass();
        for (String param : params) {
            String methodName = "get" + StringUtils.capitalize(param);

            Method method;
            try {
                method = clazz.getMethod(methodName);
                method.setAccessible(true);
                Object invokeResult = method.invoke(obj);

                if (invokeResult instanceof String) {
                    result.put(param, (String) invokeResult);
                } else {
                    result.put(param, invokeResult.toString());
                }

            } catch (Exception e) {
                logger.error("Exception obtaining Object attribute, current param=" + param, e);
            }
        }
        return result;
    }

    /**
     * 指定对象实例，返回对象的序列化字符串
     *
     * @param obj 需要序列化的对象
     * @return
     */
    public static String toJson(Object obj) {
        try {
            return JSONObject.toJSONString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    public static <T> T getDOByClass(List<Object> objs, Class<T> clazz) {
        for (Object obj : objs) {
            if (obj.getClass() == clazz) {
                return (T) obj;
            }
        }
        return null;
    }
}
