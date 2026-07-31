package io.github.loadup.gateway.starter;

import io.github.loadup.gateway.core.engine.DefaultGatewayEngine;
import io.github.loadup.gateway.core.engine.GatewayEngine;
import io.github.loadup.gateway.core.filter.BodyParserFilter;
import io.github.loadup.gateway.core.filter.CircuitBreakerFilter;
import io.github.loadup.gateway.core.filter.ExceptionFilter;
import io.github.loadup.gateway.core.filter.ProxyFilter;
import io.github.loadup.gateway.core.filter.RateLimitFilter;
import io.github.loadup.gateway.core.filter.ResponseWrapperFilter;
import io.github.loadup.gateway.core.filter.SecurityFilter;
import io.github.loadup.gateway.core.filter.TracingFilter;
import io.github.loadup.gateway.core.handler.GatewayHandlerAdapter;
import io.github.loadup.gateway.core.handler.GatewayHandlerMapping;
import io.github.loadup.gateway.core.plugin.PluginManager;
import io.github.loadup.gateway.core.router.RouteResolver;
import io.github.loadup.gateway.core.security.SecurityStrategyManager;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.spi.GatewayFilter;
import io.github.loadup.gateway.facade.spi.ProxyProcessor;
import io.github.loadup.gateway.facade.spi.RouteStore;
import io.github.loadup.gateway.facade.spi.SecurityStrategy;
import io.github.loadup.gateway.plugins.yaml.YamlRouteStore;
import io.github.loadup.gateway.plugins.yaml.event.RouteStoreRefreshedEvent;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

@AutoConfiguration
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(GatewayAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public YamlRouteStore routeStore(GatewayProperties props, ApplicationEventPublisher publisher) {
        return new YamlRouteStore(props, publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public RouteResolver routeResolver(RouteStore routeStore, GatewayProperties props) {
        return new RouteResolver(routeStore, props);
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginManager pluginManager(List<ProxyProcessor> proxyProcessors) {
        return new PluginManager(proxyProcessors);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityStrategyManager securityStrategyManager(
            @Autowired(required = false) List<SecurityStrategy> strategies) {
        return new SecurityStrategyManager(strategies);
    }

    // --- Filters ---

    @Bean
    @ConditionalOnMissingBean
    public ExceptionFilter exceptionFilter() {
        return new ExceptionFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public ProxyFilter proxyFilter(PluginManager pm) {
        return new ProxyFilter(pm);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseWrapperFilter responseWrapperFilter(GatewayProperties p) {
        return new ResponseWrapperFilter(p);
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitFilter rateLimitFilter(GatewayProperties p) {
        return new RateLimitFilter(p);
    }

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerFilter circuitBreakerFilter(GatewayProperties p) {
        return new CircuitBreakerFilter(p);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilter securityFilter(SecurityStrategyManager m) {
        return new SecurityFilter(m);
    }

    @Bean
    @ConditionalOnMissingBean
    public BodyParserFilter bodyParserFilter() {
        return new BodyParserFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(Tracer.class)
    @ConditionalOnProperty(prefix = "loadup.tracer", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TracingFilter tracingFilter(
            @Autowired(required = false) Tracer t, @Autowired(required = false) TextMapPropagator p) {
        if (t == null || p == null) {
            log.warn("Tracing disabled");
            return null;
        }
        return new TracingFilter(t, p);
    }

    // --- Security Strategies ---

    @Bean
    @ConditionalOnMissingBean(name = "defaultSecurityStrategy")
    public SecurityStrategy defaultSecurityStrategy(GatewayProperties p) {
        return new io.github.loadup.gateway.core.security.DefaultSecurityStrategy(p);
    }

    @Bean
    @ConditionalOnMissingBean(name = "internalSecurityStrategy")
    public SecurityStrategy internalSecurityStrategy() {
        return new io.github.loadup.gateway.core.security.InternalSecurityStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(name = "signatureSecurityStrategy")
    public SecurityStrategy signatureSecurityStrategy(GatewayProperties p) {
        return new io.github.loadup.gateway.core.security.SignatureSecurityStrategy(p);
    }

    // --- Engine ---

    @Bean
    @ConditionalOnMissingBean
    public DefaultGatewayEngine gatewayEngine(
            List<GatewayFilter> filters,
            RouteResolver routeResolver,
            RouteStore routeStore,
            ExceptionFilter exceptionFilter,
            @Autowired(required = false) TracingFilter tracingFilter,
            ProxyFilter proxyFilter,
            ResponseWrapperFilter responseWrapperFilter) {
        Map<String, GatewayFilter> registry =
                filters.stream().collect(Collectors.toMap(GatewayFilter::name, Function.identity(), (a, b) -> a));
        log.info("Gateway filter registry: {}", registry.keySet());
        return new DefaultGatewayEngine(
                registry,
                routeResolver,
                routeStore,
                exceptionFilter,
                tracingFilter,
                proxyFilter,
                responseWrapperFilter);
    }

    // --- Handlers ---

    @Bean
    @ConditionalOnMissingBean
    public GatewayHandlerAdapter gatewayHandlerAdapter(GatewayEngine engine) {
        return new GatewayHandlerAdapter(engine);
    }

    @Bean
    @ConditionalOnMissingBean
    public GatewayHandlerMapping gatewayHandlerMapping(RouteResolver routeResolver) {
        return new GatewayHandlerMapping(routeResolver);
    }

    @EventListener
    public void onRouteStoreRefreshed(RouteStoreRefreshedEvent event) {
        log.info("YAML routes changed, refreshing resolver");
    }
}
