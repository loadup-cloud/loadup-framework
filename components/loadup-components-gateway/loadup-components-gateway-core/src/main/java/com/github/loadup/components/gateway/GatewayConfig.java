package com.github.loadup.components.gateway;

import com.github.loadup.components.gateway.core.communication.web.GatewayFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    @Bean
    public GatewayFilter filterBean() {
        return new GatewayFilter();
    }

    @Bean
    public FilterRegistrationBean<GatewayFilter> gatewayFilter() {
        FilterRegistrationBean<GatewayFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filterBean());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}