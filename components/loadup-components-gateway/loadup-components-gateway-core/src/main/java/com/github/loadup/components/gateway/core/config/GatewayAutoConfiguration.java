package com.github.loadup.components.gateway.core.config;

import com.github.loadup.components.gateway.core.filter.GatewayFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;

/**
 * Gateway Auto Configuration
 * 网关自动配置类
 */
@Configuration
public class GatewayAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public List<GatewayFilter> sortedFilters(List<GatewayFilter> filters) {
        // 按照order排序过滤器
        filters.sort(Comparator.comparing(GatewayFilter::getOrder));
        return filters;
    }
}
