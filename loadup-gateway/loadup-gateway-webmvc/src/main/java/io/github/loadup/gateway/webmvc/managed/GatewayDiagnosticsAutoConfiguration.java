package io.github.loadup.gateway.webmvc.managed;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureAfter(name = "io.github.loadup.gateway.webmvc.autoconfigure.GatewayWebMvcAutoConfiguration")
@ConditionalOnClass(Endpoint.class)
@ConditionalOnBean(ManagedRouteRegistry.class)
public class GatewayDiagnosticsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public GatewayRoutesEndpoint gatewayRoutesEndpoint(ManagedRouteRegistry registry) {
        return new GatewayRoutesEndpoint(registry);
    }
}
