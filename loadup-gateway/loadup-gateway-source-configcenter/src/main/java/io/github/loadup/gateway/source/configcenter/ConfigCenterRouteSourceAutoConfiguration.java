package io.github.loadup.gateway.source.configcenter;

import io.github.loadup.components.configcenter.ConfigCenterTemplate;
import io.github.loadup.gateway.api.spi.RouteSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureAfter(name = "io.github.loadup.components.configcenter.autoconfig.ConfigCenterAutoConfiguration")
@AutoConfigureBefore(name = "io.github.loadup.gateway.starter.GatewayAutoConfiguration")
@ConditionalOnProperty(prefix = "loadup.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "loadup.gateway.source", name = "type", havingValue = "configcenter")
public class ConfigCenterRouteSourceAutoConfiguration {
    @Bean
    @ConditionalOnBean(ConfigCenterTemplate.class)
    @ConditionalOnMissingBean(RouteSource.class)
    public RouteSource configCenterRouteSource(
            ConfigCenterTemplate config,
            ApplicationEventPublisher events,
            @Value("${loadup.gateway.source.configcenter.key:gateway-routes}") String key,
            @Value("${loadup.gateway.route-refresh-interval:5}") int pollSeconds) {
        return new ConfigCenterRouteSource(config, key, pollSeconds, events);
    }
}
