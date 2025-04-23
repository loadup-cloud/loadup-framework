package com.alipay.test.acts.component.components;

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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alipay.test.acts.annotation.TestComponent;
import com.alipay.test.acts.utils.config.ConfigrationFactory;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class ActsComponentUtil {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ActsComponentUtil.class);

    /** 组件回调 */
    public static ThreadLocal<Map<String, ActsComponentInvoker>> actsComponentsHolder = new ThreadLocal<Map<String, ActsComponentInvoker>>();

    public static void init(String componentPackage, ClassLoader loader) {
        if (actsComponentsHolder.get() != null) {
            return;
        }
        Map<String, ActsComponentInvoker> actsComponents = new LinkedHashMap<String, ActsComponentInvoker>();
        actsComponentsHolder.set(actsComponents);

        //加载components
        ImmutableSet<ClassInfo> classes = null;
        try {
            classes = ClassPath.from(loader).getTopLevelClasses();
            if (classes != null) {
                for (final ClassPath.ClassInfo info : classes) {
                    if (info.getName().startsWith(componentPackage)) {
                        String name = info.getName();
                        Class<?> clazz = Class.forName(name);
                        Method[] methods = clazz.getMethods();
                        for (Method method : methods) {

                            if (method.isAnnotationPresent(TestComponent.class)) {
                                ActsComponentInvoker actsComponentInvoker = new ActsComponentInvoker();
                                actsComponentInvoker.setTargetMethod(method);
                                actsComponentInvoker.setComponentObject(clazz.newInstance());
                                actsComponents.put(
                                    (method.getAnnotation(TestComponent.class)).id(),
                                    actsComponentInvoker);
                            }

                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("acts components init error!", e);
        }
        /***
         * 兼容原来的逻辑，ClassPath在sofa4上无法从KernelAceClassLoader中获取相应的package
         */
        if (actsComponents.isEmpty()) {
            try {
                String components = ConfigrationFactory.getConfigration().getPropertyValue(
                        "param_components");
                if (StringUtils.isBlank(components)) {
                    return;
                }
                for (String comp : components.split(",")) {
                    //剔除所有空格防止拼接的name不合法
                    if (StringUtils.isEmpty((comp = comp.trim()))) {
                        continue;
                    }
                    String classQualifyName = componentPackage+"."+comp;
                    Class<?> cl = Class.forName(classQualifyName);
                    Method[] methods = cl.getMethods();

                    for (Method method : methods) {
                        if (method.isAnnotationPresent(TestComponent.class)) {
                            ActsComponentInvoker componentInvoker = new ActsComponentInvoker();
                            componentInvoker.setComponentObject(cl.newInstance());
                            componentInvoker.setTargetMethod(method);
                            actsComponents.put((method.getAnnotation(TestComponent.class)).id(),
                                    componentInvoker);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("acts components init error!", e);
            }
        }
    }

    public static Object run(String command) {
        String id = "";
        //判断是否是有入参的方法.
        if (StringUtils.contains(command, "?")) {
            id = StringUtils.substringBetween(command, "@", "?");
        } else {
            id = StringUtils.substringAfter(command, "@");
        }
        try {
            return actsComponentsHolder.get().get(id).execute(command);
        } catch (Exception e) {
            LOGGER.error("acts components run error!", e);
        }
        return null;
    }

    public static void clear() {
        actsComponentsHolder.remove();
    }
}
