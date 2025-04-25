/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.support;

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

import org.springframework.context.ApplicationContext;

/**
 * sofa 环境上下文引用。
 * <p>
 * 依赖sofa版本，可获取bundle bean。
 *
 * 
 * 
 */
public class SofaRuntimeReference {

    /**
     * sofa组件上下文
     */
    private static final ThreadLocal<Object> runtimeLocal = new ThreadLocal<>();

    /**
     * spring 上下文，覆盖不存在sofa上下文的场景
     */
    private static volatile ApplicationContext applicationContext = null;

    public static Object getBean(String name, Class<?> requiredType, String moduleName) {

//        Collection<ComponentInfo> componentInfos = (Collection<ComponentInfo>) getCurrentSofaRuntime()
//            .getComponentManager().getComponents();
//
//        for (ComponentInfo componentInfo : componentInfos) {
//            Object bean = null;
//            try {
//                ApplicationContext applicationContext = (ApplicationContext) (componentInfo
//                    .getImplementation().getTarget());
//                if (applicationContext != null) {
//                    if (!StringUtils.isEmpty(moduleName)
//                        && !componentInfo.dump().contains(moduleName)) {
//                        continue;
//                    }
//                    if (name != null && requiredType != null) {
//                        bean = applicationContext.getBean(name, requiredType);
//                    } else if (name != null) {
//                        bean = applicationContext.getBean(name);
//                    } else if (requiredType != null) {
//                        bean = applicationContext.getBean(requiredType);
//                    }
//                }
//            } catch (Exception e) {
//            }
//            if (bean != null) {
//                TestLogger.getLogger().info("find bean in bundle[name=", name, ",type=",
//                    requiredType, ",bundle=", componentInfo.dump(), "]");
//                return bean;
//            }
//        }
        return null;
    }

    public static Object getBean(String name, String moduleName) {
        if (moduleName == null) {
            return getBean(name);
        }
        return getBean(name, null, moduleName);
    }

    public static Object getBean(String name) {
        if (!isSofaRuntimeEnable()) {
            if (applicationContext != null) {
                return applicationContext.getBean(name);
            }
        }
        return getBean(name, null, null);
    }

    public static Object getBean(Class<?> type) {

        if (!isSofaRuntimeEnable()) {
            if (applicationContext != null) {
                return applicationContext.getBean(type);
            }
        }

        return getBean(null, type, null);
    }

    /**
     * 设置spring上下文
     *
     * @param applicationContext
     */
    public static void setApplicationContext(ApplicationContext applicationContext) {
        SofaRuntimeReference.applicationContext = applicationContext;
    }

    /**
     * 获取上下文。
     *
     * @return
     */
    private static Object getCurrentSofaRuntime() {
        Object sofaRuntime = runtimeLocal.get();

        if (sofaRuntime == null) {
            throw new RuntimeException("runtime not init yet!");
        }

        return sofaRuntime;
    }

    /**
     * 设置上下文。
     *
     * @param runtime
     */
    public static void setCurrentSofaRuntime(Object runtime) {
        runtimeLocal.set(runtime);
    }

    /**
     * 是否sofa上下文可用。
     *
     * @return
     */
    private static boolean isSofaRuntimeEnable() {

        return runtimeLocal.get() != null;
    }

}
