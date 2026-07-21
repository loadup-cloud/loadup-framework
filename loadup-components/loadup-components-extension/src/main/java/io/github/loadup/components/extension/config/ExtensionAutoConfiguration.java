package io.github.loadup.components.extension.config;

/*-
 * #%L
 * Loadup Components Extension
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

import io.github.loadup.components.extension.annotation.BizScenario;
import io.github.loadup.components.extension.exector.ExtensionExecutor;
import io.github.loadup.components.extension.interceptor.BizScenarioInterceptor;
import io.github.loadup.components.extension.register.ExtensionRegistry;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Extension 框架的 Spring Boot 自动装配类
 */
@AutoConfiguration
@ConditionalOnClass({ExtensionExecutor.class, ExtensionRegistry.class})
public class ExtensionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExtensionRegistry extensionRegistry() {
        return new ExtensionRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExtensionExecutor extensionExecutor(ExtensionRegistry extensionRegistry) {
        return new ExtensionExecutor(extensionRegistry);
    }

    /**
     * 注册 {@link BizScenario @BizScenario} AOP 拦截器。
     *
     * <p>拦截所有标注了 {@code @BizScenario} 的 Service 方法，在方法执行前后自动管理
     * {@link io.github.loadup.components.extension.context.BizContextHolder} 的生命周期。
     */
    @Bean
    @ConditionalOnMissingBean(BizScenarioInterceptor.class)
    public BizScenarioInterceptor bizScenarioInterceptor() {
        return new BizScenarioInterceptor();
    }

    @Bean
    public Advisor bizScenarioAdvisor(BizScenarioInterceptor bizScenarioInterceptor) {
        AnnotationMatchingPointcut pointcut = new AnnotationMatchingPointcut(null, BizScenario.class, true);
        return new DefaultPointcutAdvisor(pointcut, bizScenarioInterceptor);
    }
}
