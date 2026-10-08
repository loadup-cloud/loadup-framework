package io.github.loadup.components.database.autoconfig;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.tenant.TenantFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/** Registers request tenant propagation for web applications. */
@AutoConfiguration
@EnableConfigurationProperties(DatabaseProperties.class)
@ConditionalOnClass(name = {"jakarta.servlet.Filter", "org.springframework.boot.web.servlet.FilterRegistrationBean"})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "loadup.database.multi-tenant", name = "enabled", havingValue = "true")
public class MultiTenantAutoConfiguration {

    @Bean
    public FilterRegistrationBean<TenantFilter> tenantFilterRegistration(DatabaseProperties properties) {
        FilterRegistrationBean<TenantFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TenantFilter(properties.getMultiTenant().getRequest()));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.setName("tenantFilter");

        LogUtil.info(MultiTenantAutoConfiguration.class, "Registered TenantFilter for multi-tenant support");
        return registration;
    }
}
