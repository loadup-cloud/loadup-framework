/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.model;

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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.components.testify.enums.YamlFieldEnum;
import com.github.loadup.components.testify.exception.TestifyException;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ClassUtils;

/**
 *
 */
@Getter
@Setter
public class CaseObjectSet {

    private String description;

    // 用例入参对象列表
    private List<Object> args;

    // 对象比对标识
    private Map<String, Map<String, String>> flags;

    // 用例返回对象（出参）
    private Object resultObj;
    // case exception
    private Map<String, String> exception;

    // 用例事件/消息列表
    private List<Map<String, Object>> events;

    // 自定义变量
    private Map<String, Object> params;

    // 组件化列表
    private List<Map<String, Object>> components;

    /**
     * 从yaml对象列表中读取相关对象集合，默认顺序为[入参列表，flags列表，返回对象，事件/消息列表，自定义变量，VirtualMock]
     *
     * @param yamlObjs
     */
    private List<Object> parseArgs(Map<String, Object> yamlObjs) {
        Map<String, Map<String, String>> argsMap =
                (Map<String, Map<String, String>>) yamlObjs.get(YamlFieldEnum.ARGS.getCode());
        if (argsMap == null) return Collections.emptyList();
        List<Object> convertedArgs = new ArrayList<>();
        argsMap.entrySet().forEach(entry -> {
            if (entry.getKey().contains(".")) {
                String className = entry.getKey();
                Class<?> aClass;
                try {
                    aClass = ClassUtils.forName(className, this.getClass().getClassLoader());
                } catch (ClassNotFoundException e) {
                    throw new TestifyException("Request class not found," + className);
                }
                Map value = entry.getValue();
                Object parsed = JsonUtil.parseObject(value, aClass);
                convertedArgs.add(parsed);
            }
        });
        return CollectionUtils.isEmpty(convertedArgs) ? Collections.emptyList() : convertedArgs;
    }

    private String extractMainClassName(String key) {
        return StringUtils.substringBefore(key, "<");
    }

    private String extractGenericTypeName(String key) {
        return StringUtils.substringBetween(key, "<", ">");
    }

    private Object parseResultObj(Map<String, Object> yamlObjs) {
        Map<String, Map<String, String>> retMap = (Map<String, Map<String, String>>)
                yamlObjs.getOrDefault(YamlFieldEnum.RESULT.getCode(), new HashMap<>());
        if (MapUtils.isEmpty(retMap)) {
            return null;
        }
        return retMap.entrySet().stream()
                .filter(entry -> entry.getKey().contains("."))
                .map(entry -> {
                    String classNameKey = entry.getKey();
                    Class<?> aClass;
                    try {
                        // 提取主类名和泛型参数
                        String className = extractMainClassName(classNameKey);
                        String genericTypeName = extractGenericTypeName(classNameKey);

                        aClass = ClassUtils.forName(className, this.getClass().getClassLoader());

                        if (StringUtils.isNotBlank(genericTypeName)) {
                            Class<?> genericType = ClassUtils.forName(
                                    genericTypeName, this.getClass().getClassLoader());
                            // 使用 TypeReference 解析带泛型的对象
                            JavaType javaType =
                                    new ObjectMapper().getTypeFactory().constructParametricType(aClass, genericType);

                            return JsonUtil.parseObject(entry.getValue(), javaType);
                        } else {
                            // 普通对象解析
                            return JsonUtil.parseObject(entry.getValue(), aClass);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new TestifyException("Class not found: " + classNameKey, e);
                    }
                })
                .findFirst()
                .orElse(null);
    }

    public CaseObjectSet(Map<String, Object> yamlObjs) {
        description = (String) yamlObjs.get(YamlFieldEnum.DESCRIPTION.getCode());
        args = parseArgs(yamlObjs);
        flags = (Map<String, Map<String, String>>)
                yamlObjs.getOrDefault(YamlFieldEnum.FLAGS.getCode(), new HashMap<>());

        resultObj = parseResultObj(yamlObjs);

        params = (Map<String, Object>) yamlObjs.getOrDefault(YamlFieldEnum.PARAMS.getCode(), new HashMap<>());

        components = null;

        exception = parseException(yamlObjs);
    }

    private Map<String, String> parseException(Map<String, Object> yamlObjs) {
        Map<String, String> exceptionMap =
                (Map<String, String>) yamlObjs.getOrDefault(YamlFieldEnum.EXCEPTION.getCode(), new HashMap<>());
        if (MapUtils.isEmpty(exceptionMap)) {
            return null;
        }
        return exceptionMap;
    }

    public boolean isException() {
        if (MapUtils.isNotEmpty(exception)) {
            return true;
        }
        return resultObj.getClass().getName().toLowerCase().endsWith("exception");
    }

    public boolean isEmpty() {
        return null == args && null == resultObj && null == events;
    }
}
