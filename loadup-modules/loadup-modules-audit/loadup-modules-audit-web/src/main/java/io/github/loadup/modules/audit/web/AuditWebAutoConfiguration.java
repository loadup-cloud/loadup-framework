/*
 * #%L
 * LoadUp Audit Center Web Adapter
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
package io.github.loadup.modules.audit.web;

import io.github.loadup.modules.audit.client.facade.AuditFacade;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Optional MVC capture and query surface for the audit center. */
@AutoConfiguration(afterName = "io.github.loadup.modules.audit.app.autoconfigure.AuditAutoConfiguration")
@ConditionalOnBean(AuditFacade.class)
@EnableConfigurationProperties(AuditWebProperties.class)
public class AuditWebAutoConfiguration {
    @Bean
    public AuditController auditController(AuditFacade service) {
        return new AuditController(service);
    }

    @Bean
    public AuditResponseAdvice auditResponseAdvice() {
        return new AuditResponseAdvice();
    }

    @Bean
    public WebMvcConfigurer auditMvcConfigurer(AuditFacade service, AuditWebProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                if (!properties.getIncludePaths().isEmpty()) {
                    registry.addInterceptor(new AuditCaptureInterceptor(service, properties))
                            .addPathPatterns(properties.getIncludePaths());
                }
            }
        };
    }
}
