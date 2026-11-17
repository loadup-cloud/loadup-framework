/*
 * Copyright (C) 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.webmvc;

import io.github.loadup.components.runtime.deployment.DeploymentAutoConfiguration;
import io.github.loadup.components.runtime.deployment.DeploymentIdentity;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Registers the deployment code check after the execution context filter and before the security
 * filter chain of the application.
 */
@AutoConfiguration(after = {DeploymentAutoConfiguration.class, LoadUpWebMvcAutoConfiguration.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnBean(DeploymentIdentity.class)
@EnableConfigurationProperties(DeploymentCodeProperties.class)
public class DeploymentCodeWebAutoConfiguration {

    @Bean
    public FilterRegistrationBean<DeploymentCodeFilter> loadUpDeploymentCodeFilter(
            DeploymentIdentity identity,
            DeploymentCodeProperties properties,
            ObjectProvider<ObjectMapper> objectMapper) {
        var filter = new DeploymentCodeFilter(
                identity,
                properties,
                objectMapper.getIfAvailable(() -> JsonMapper.builder().build()));
        var registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
        registration.setName("loadUpDeploymentCodeFilter");
        registration.setAsyncSupported(true);
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR);
        return registration;
    }
}
