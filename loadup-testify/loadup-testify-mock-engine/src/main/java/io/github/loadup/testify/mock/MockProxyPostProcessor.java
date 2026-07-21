package io.github.loadup.testify.mock;

/*-
 * #%L
 * Testify Mock Engine
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.testify.mock.engine.MockInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Service;

public class MockProxyPostProcessor implements BeanPostProcessor {
    private final MockInterceptor interceptor;

    public MockProxyPostProcessor(MockInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public MockInterceptor getInterceptor() {
        return interceptor;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 健壮性判断：只拦截你感兴趣的业务类
        if (shouldProxy(bean, beanName)) {
            ProxyFactory factory = new ProxyFactory(bean);
            factory.addAdvice(interceptor);
            // 必须：强制使用 CGLIB，防止接口注入失效
            factory.setProxyTargetClass(true);
            Object proxy = factory.getProxy();
            return proxy;
        }
        return bean;
    }

    private boolean shouldProxy(Object bean, String beanName) {
        // 拿到真实的类，穿透 Spring 已有的代理（如事务、异步）
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        String className = targetClass.getName();

        // 策略 B：智能探测
        // 1. 根据 Spring 标准注解识别
        boolean hasServiceAnnotation = AnnotatedElementUtils.hasAnnotation(targetClass, Service.class);

        // 2. 根据命名规范识别（支持业务 Service、FeignClient、MyBatis Mapper 等常见业务组件）
        boolean isBusinessService = className.endsWith("Service")
                || className.endsWith("ServiceImpl")
                || className.contains(".service.") // 包含 service 包路径
                || className.contains(".manager."); // 包含 manager 包路径

        // 3. 排除 Spring 系统自带的 Bean (通常以 org.springframework 开头)
        boolean isSystemBean = className.startsWith("org.springframework")
                || className.startsWith("java.")
                || className.startsWith("javax.")
                || className.startsWith("jakarta.");

        return (hasServiceAnnotation || isBusinessService) && !isSystemBean;
    }
}
