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

import com.github.loadup.components.testify.log.TestifyLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;

/**
 * bean装载工具类
 *
 *
 *
 */
@Slf4j
public class BeanUtil {

    /**
     * 基于名称和module加载bean
     *
     * @param name
     * @param moduleName
     * @return
     */
    public static Object getBean(String name, String moduleName) {
        if (moduleName == null || moduleName.length() == 0) {
            moduleName = "ON_THE_FLY_BUNDLE_NAME";
        }
        //        Collection<ComponentInfo> componentInfos = SofaRunTimeContextHolder.get()
        //            .getComponentManager().getComponents();
        //        for (ComponentInfo componentInfo : componentInfos) {
        //            try {
        //                ApplicationContext applicationContext = (ApplicationContext)
        // (componentInfo
        //                    .getImplementation().getTarget());
        //                if (applicationContext != null &&
        // componentInfo.dump().contains(moduleName)) {
        //                    if (null != applicationContext.getBean(name)) {
        //                        return applicationContext.getBean(name);
        //                    }
        //                }
        //            } catch (Exception e) {
        //                //这里轮询查找bean允许出现强转异常，不打印错误日志
        //                continue;
        //            }
        //        }
        return null;
    }

    public static Object getTargetBean(Object bean) {
        Object targetBean = bean;
        if (bean == null) {
            TestifyLogUtil.error(log, "目标Bean未被注入进来,请检查.");
            return targetBean;
        }

        while (targetBean instanceof Advised) {
            try {
                Object target = ((Advised) targetBean).getTargetSource().getTarget();
                if (target == targetBean) {
                    // 防止陷入死循环中
                    break;
                } else {
                    targetBean = target;
                }
            } catch (Exception e) {
                TestifyLogUtil.error(log, "获取target时发生异常.", e);
            }
        }
        return targetBean;
    }

    public static Object getBean(String beanName) {
        // 规则为 {bundlename};{beanname}
        String[] parts = beanName.split(";");
        String bundleName = "ON_THE_FLY_BUNDLE_NAME";
        if (2 == parts.length) {
            bundleName = parts[0];
            beanName = parts[1];
        }
        return getBean(beanName, bundleName);
    }
}
