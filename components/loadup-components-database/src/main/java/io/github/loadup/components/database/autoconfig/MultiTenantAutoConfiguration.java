package io.github.loadup.components.database.autoconfig;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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

import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.tenant.TenantFilter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(DatabaseProperties.class)
@ConditionalOnProperty(
    prefix = "loadup.database.multi-tenant",
    name = "enabled",
    havingValue = "true")
public class MultiTenantAutoConfiguration {

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
