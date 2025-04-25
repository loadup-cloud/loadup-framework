/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.datarule.handler;

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

import com.alibaba.fastjson2.JSON;
import com.github.loadup.components.testify.datarule.CustomRule;
import com.github.loadup.components.testify.datarule.RULE;
import com.github.loadup.components.testify.datarule.RULE.ReferenceHandler;
import com.github.loadup.components.testify.datarule.Reference;
import com.github.loadup.components.testify.datarule.RuleObject;
import com.github.loadup.components.testify.exception.RuleExecuteException;
import com.github.loadup.components.testify.util.CustomUtils;
import com.github.loadup.components.testify.util.DateUtil;
import com.github.loadup.components.testify.util.SystemAssist;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义规则处理。
 *
 * 
 *
 */
public class CustomRuleHandler implements RuleHandler<CustomRule> {

    /**
     * 自定义类名
     */
    private static final String CUSTOM_CLASS_NAME = ".servicetest.base.CustomUtils";

    /**
     * 框架自定义类
     */
    private static final List<Class<?>> customFrameworkClasses = new ArrayList<Class<?>>();

    /**
     * 系统自定义类
     */
    private static final Map<String, List<Class<?>>> customSystemClasses = new HashMap<String, List<Class<?>>>();

    static {
        customFrameworkClasses.add(CustomUtils.class);
        customFrameworkClasses.add(DateUtil.class);
    }

    /**
     * @see RuleHandler#handle(RuleObject, ReferenceHandler)
     */
    @Override
    public String handle(CustomRule rule, ReferenceHandler refHandler) {

        Method method = getMethod(refHandler == null ? null : refHandler.getSystem(), rule);

        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Object param = rule.getParams().get(i);
            if (param instanceof Reference) {

                String name = ((Reference) param).getName();
                param = refHandler.getData(name);
                if (param == null) {
                    String refRule = refHandler.getRule(name);
                    param = RULE.execute(refRule, refHandler);
                    refHandler.setData(name, (String) param);
                }
            }

            params[i] = convertParam(param, paramTypes[i]);
        }

        try {
            Object value = method.invoke(null, params);
            return convertResult(value);
        } catch (Exception e) {
            throw new RuleExecuteException("custom rule execute error,method invoke error[method="
                    + method.toGenericString() + "]", e);
        }
    }

    /**
     * 获取可执行方法。
     *
     * @param system
     * @param rule
     * @return
     */
    private Method getMethod(String system, CustomRule rule) {
        String methodName = rule.getMethodName();
        int paramSize = rule.getParams().size();

        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.addAll(getCustomSystemClasses(system));
        classes.addAll(customFrameworkClasses);

        for (Class<?> clazz : classes) {
            Method[] methods = ReflectionUtils.getAllDeclaredMethods(clazz);
            for (Method method : methods) {
                if (methodName.equals(method.getName())
                        && paramSize == method.getParameterTypes().length) {
                    return method;
                }
            }
        }

        throw new RuleExecuteException("custom rule execute error,can't find method[name="
                + rule.getMethodName() + ",paramSize=" + paramSize + "]");
    }

    /**
     * 获取自定义的类。
     *
     * @param system
     * @return
     */
    private List<Class<?>> getCustomSystemClasses(String system) {
        List<Class<?>> classes = customSystemClasses.get(system);
        if (classes != null) {
            return classes;
        }

        classes = new ArrayList<Class<?>>();
        List<String> packages = SystemAssist.getPossiblePackageList(system);
        for (String pack : packages) {
            try {
                classes.add(Class.forName(pack + CUSTOM_CLASS_NAME, true, Thread.currentThread()
                        .getContextClassLoader()));
            } catch (Exception e) {
                //TODO:临时注释
                //TestLogger.getLogger().info("not find custom class", pack, CUSTOM_CLASS_NAME);
            }
        }

        customSystemClasses.put(system, classes);

        return classes;
    }

    /**
     * 参数转换。
     *
     * @param originParam
     * @param paramType
     * @return
     */
    private Object convertParam(Object originParam, Class<?> paramType) {

        if (paramType.isInstance(originParam)) {
            return originParam;
        }

        if (originParam instanceof String) {
            String json = (String) originParam;
            if (!json.startsWith("{") && !json.startsWith("\"") && !json.startsWith("'")) {
                json = "\"" + json + "\"";
            }
            return JSON.parseObject(json, paramType);
        }

        return originParam;
    }

    /**
     * 结果转换。
     *
     * @param value
     * @return
     */
    private String convertResult(Object value) {

        if (value == null) {
            return null;
        }

        if (value.getClass().isPrimitive()) {
            return value.toString();
        }

        if (value instanceof String) {
            return (String) value;
        }

//        if (value instanceof Date) {
//            return JSON.toJSONStringWithDateFormat(value, JSON.DEFFAULT_DATE_FORMAT);
//        }

        return JSON.toJSONString(value);
    }
}
