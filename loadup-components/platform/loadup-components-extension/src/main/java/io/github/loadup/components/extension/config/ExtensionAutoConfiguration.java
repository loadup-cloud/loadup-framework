/*
 * #%L
 * Loadup Components Extension
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.components.extension.config;

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
