package io.github.loadup.components.database.autoconfig;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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

import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.tenant.TenantFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Multi-Tenant Auto Configuration
 *
 * <p>Registers TenantFilter for web applications when multi-tenant is enabled
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(DatabaseProperties.class)
@ConditionalOnProperty(prefix = "loadup.database.multi-tenant", name = "enabled", havingValue = "true")
public class MultiTenantAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(MultiTenantAutoConfiguration.class);

    @Bean
    @ConditionalOnWebApplication
    public FilterRegistrationBean<TenantFilter> tenantFilterRegistration() {
        FilterRegistrationBean<TenantFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TenantFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.setName("tenantFilter");

        log.info("Registered TenantFilter for multi-tenant support");
        return registration;
    }
}
