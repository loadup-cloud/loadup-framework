/*-
 * #%L
 * Loadup Gateway WebMVC Engine
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
package io.github.loadup.gateway.webmvc.autoconfigure;

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.spi.ProxyProcessor;
import io.github.loadup.gateway.facade.spi.RouteStore;
import io.github.loadup.gateway.facade.spi.SecurityStrategy;
import io.github.loadup.gateway.webmvc.exception.GatewayExceptionHandler;
import io.github.loadup.gateway.webmvc.filter.CircuitBreakerHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.filter.RateLimitHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.filter.ResponseWrapperHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.filter.SecurityHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.filter.TracingHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.proxy.ProxyHandlerFunction;
import io.github.loadup.gateway.webmvc.proxy.ProxyProcessorRegistry;
import io.github.loadup.gateway.webmvc.router.RouteFunctionRegistry;
import io.github.loadup.gateway.webmvc.security.DefaultSecurityStrategy;
import io.github.loadup.gateway.webmvc.security.InternalSecurityStrategy;
import io.github.loadup.gateway.webmvc.security.SecurityStrategyManager;
import io.github.loadup.gateway.webmvc.security.SignatureSecurityStrategy;
import io.opentelemetry.api.trace.Tracer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;

/**
 * Spring Boot auto-configuration for the LoadUp gateway MVC engine.
 *
 * <p>Exposes the gateway as a {@link RouterFunction} bean that Spring MVC's
 * {@code RouterFunctionMapping} picks up automatically, so gateway routes coexist with
 * regular {@code @RestController} mappings. Route definitions come from an existing
 * {@link RouteStore} bean (YAML / database plugin).
 */
@AutoConfiguration
@EnableConfigurationProperties(GatewayProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({RouterFunction.class, MvcUtils.class})
@ConditionalOnProperty(prefix = "loadup.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GatewayWebMvcAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(GatewayWebMvcAutoConfiguration.class);

    // --- Security strategies ---

    @Bean
    @ConditionalOnMissingBean(name = "defaultSecurityStrategy")
    public SecurityStrategy defaultSecurityStrategy(GatewayProperties properties) {
        return new DefaultSecurityStrategy(properties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "internalSecurityStrategy")
    public SecurityStrategy internalSecurityStrategy() {
        return new InternalSecurityStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(name = "signatureSecurityStrategy")
    public SecurityStrategy signatureSecurityStrategy(GatewayProperties properties) {
        return new SignatureSecurityStrategy(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityStrategyManager securityStrategyManager(
            @Autowired(required = false) List<SecurityStrategy> strategies) {
        return new SecurityStrategyManager(strategies);
    }

    // --- Proxy ---

    @Bean
    @ConditionalOnMissingBean
    public ProxyProcessorRegistry proxyProcessorRegistry(
            @Autowired(required = false) List<ProxyProcessor> proxyProcessors) {
        return new ProxyProcessorRegistry(proxyProcessors);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProxyHandlerFunction proxyHandlerFunction(ProxyProcessorRegistry proxyProcessorRegistry) {
        return new ProxyHandlerFunction(proxyProcessorRegistry);
    }

    // --- Filters ---

    @Bean
    @ConditionalOnMissingBean
    public GatewayExceptionHandler gatewayExceptionHandler() {
        return new GatewayExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityHandlerFilterFunction securityHandlerFilterFunction(SecurityStrategyManager strategyManager) {
        return new SecurityHandlerFilterFunction(strategyManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitHandlerFilterFunction rateLimitHandlerFilterFunction(GatewayProperties properties) {
        return new RateLimitHandlerFilterFunction(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerHandlerFilterFunction circuitBreakerHandlerFilterFunction() {
        return new CircuitBreakerHandlerFilterFunction();
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseWrapperHandlerFilterFunction responseWrapperHandlerFilterFunction(GatewayProperties properties) {
        return new ResponseWrapperHandlerFilterFunction(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(Tracer.class)
    public TracingHandlerFilterFunction tracingHandlerFilterFunction(@Autowired(required = false) Tracer tracer) {
        if (tracer == null) {
            log.info("OpenTelemetry Tracer not present, gateway tracing disabled");
            return null;
        }
        return new TracingHandlerFilterFunction(tracer);
    }

    // --- Engine entry point ---

    @Bean
    @ConditionalOnMissingBean
    public RouteFunctionRegistry gatewayRouterFunction(
            RouteStore routeStore,
            GatewayProperties properties,
            ProxyHandlerFunction proxyHandlerFunction,
            GatewayExceptionHandler gatewayExceptionHandler,
            @Autowired(required = false) TracingHandlerFilterFunction tracingHandlerFilterFunction,
            SecurityHandlerFilterFunction securityHandlerFilterFunction,
            RateLimitHandlerFilterFunction rateLimitHandlerFilterFunction,
            CircuitBreakerHandlerFilterFunction circuitBreakerHandlerFilterFunction,
            ResponseWrapperHandlerFilterFunction responseWrapperHandlerFilterFunction) {
        return new RouteFunctionRegistry(
                routeStore,
                properties,
                proxyHandlerFunction,
                gatewayExceptionHandler,
                tracingHandlerFilterFunction,
                securityHandlerFilterFunction,
                rateLimitHandlerFilterFunction,
                circuitBreakerHandlerFilterFunction,
                responseWrapperHandlerFilterFunction);
    }
}
