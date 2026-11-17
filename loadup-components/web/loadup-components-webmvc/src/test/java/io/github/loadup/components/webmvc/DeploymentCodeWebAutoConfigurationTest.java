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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.runtime.deployment.DeploymentAutoConfiguration;
import io.github.loadup.components.runtime.deployment.DeploymentIdentity;
import jakarta.servlet.DispatcherType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;

class DeploymentCodeWebAutoConfigurationTest {

    @Test
    void registersTheDeploymentCodeFilterWithoutDatabaseAccess() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        DeploymentAutoConfiguration.class, DeploymentCodeWebAutoConfiguration.class))
                .withPropertyValues("loadup.deployment.code=customer-a")
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(DeploymentIdentity.class);
                    FilterRegistrationBean<?> registration =
                            context.getBean("loadUpDeploymentCodeFilter", FilterRegistrationBean.class);
                    assertThat(registration.getFilter()).isInstanceOf(DeploymentCodeFilter.class);
                    assertThat(registration.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 5);
                    assertThat(registration.getUrlPatterns()).containsExactly("/*");
                    assertThat(registration.isAsyncSupported()).isTrue();
                    assertThat(registration.determineDispatcherTypes())
                            .containsExactlyInAnyOrder(
                                    DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR);
                });
    }

    @Test
    void bindsExemptionsFromConfigurationProperties() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        DeploymentAutoConfiguration.class, DeploymentCodeWebAutoConfiguration.class))
                .withPropertyValues(
                        "loadup.deployment.code=customer-a",
                        "loadup.webmvc.deployment-code.excluded-paths=/api/callback/**")
                .run(context -> assertThat(
                                context.getBean(DeploymentCodeProperties.class).getExcludedPaths())
                        .containsExactly("/api/callback/**"));
    }

    @Test
    void nonServletRuntimeDoesNotRegisterAWebFilter() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        DeploymentAutoConfiguration.class, DeploymentCodeWebAutoConfiguration.class))
                .withPropertyValues("loadup.deployment.code=customer-a")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean("loadUpDeploymentCodeFilter");
                });
    }
}
