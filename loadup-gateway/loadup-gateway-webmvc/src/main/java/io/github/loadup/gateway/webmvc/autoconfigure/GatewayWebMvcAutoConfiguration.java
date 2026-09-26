package io.github.loadup.gateway.webmvc.autoconfigure;

import io.github.loadup.gateway.api.spi.RouteSource;
import io.github.loadup.gateway.webmvc.config.GatewayProperties;
import io.github.loadup.gateway.webmvc.managed.CircuitBreakerFilterAdapter;
import io.github.loadup.gateway.webmvc.managed.HttpTargetAdapter;
import io.github.loadup.gateway.webmvc.managed.ManagedFilterAdapter;
import io.github.loadup.gateway.webmvc.managed.ManagedRouteRegistry;
import io.github.loadup.gateway.webmvc.managed.RewritePathFilterAdapter;
import io.github.loadup.gateway.webmvc.managed.ServiceMethodCatalog;
import io.github.loadup.gateway.webmvc.managed.ServiceTargetAdapter;
import io.github.loadup.gateway.webmvc.managed.SetRequestHeaderFilterAdapter;
import io.github.loadup.gateway.webmvc.managed.StripPrefixFilterAdapter;
import io.github.loadup.gateway.webmvc.managed.TargetHandlerAdapter;
import io.github.loadup.gateway.webmvc.security.LocalSignatureNonceStore;
import io.github.loadup.gateway.webmvc.security.RequestSignatureVerifier;
import io.github.loadup.gateway.webmvc.security.SignatureNonceStore;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Validator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.cloud.gateway.server.mvc.config.GatewayMvcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import tools.jackson.databind.ObjectMapper;

/** Registers the managed SCG MVC router and exposed service method catalog. */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({RouterFunction.class, MvcUtils.class})
@ConditionalOnProperty(prefix = "loadup.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayWebMvcAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ServiceMethodCatalog serviceMethodCatalog(
            ListableBeanFactory beans,
            Validator validator,
            ObjectMapper mapper,
            @Value("${loadup.gateway.limits.service-body-bytes:1048576}") int maxBodyBytes) {
        return new ServiceMethodCatalog(beans, validator, mapper, maxBodyBytes);
    }

    @Bean
    @ConditionalOnMissingBean
    public ManagedRouteRegistry gatewayRouterFunction(
            RouteSource routeSource,
            List<TargetHandlerAdapter> targets,
            List<ManagedFilterAdapter> filters,
            ObjectProvider<MeterRegistry> meters,
            RequestSignatureVerifier signatures,
            ObjectProvider<GatewayMvcProperties> scgProperties) {
        Set<String> reservedIds = new HashSet<>();
        GatewayMvcProperties staticRoutes = scgProperties.getIfAvailable();
        if (staticRoutes != null) {
            staticRoutes.getRoutes().forEach(route -> {
                if (route.getId() != null) {
                    reservedIds.add(route.getId());
                }
            });
            staticRoutes.getRoutesMap().forEach((key, route) -> {
                reservedIds.add(key);
                if (route.getId() != null) {
                    reservedIds.add(route.getId());
                }
            });
        }
        return new ManagedRouteRegistry(
                routeSource, targets, filters, meters.getIfAvailable(), signatures, reservedIds);
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestSignatureVerifier requestSignatureVerifier(GatewayProperties properties, SignatureNonceStore nonces) {
        return new RequestSignatureVerifier(properties, nonces);
    }

    @Bean
    @ConditionalOnMissingBean(SignatureNonceStore.class)
    public SignatureNonceStore signatureNonceStore() {
        return new LocalSignatureNonceStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceTargetAdapter serviceTargetAdapter(ServiceMethodCatalog catalog) {
        return new ServiceTargetAdapter(catalog);
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpTargetAdapter httpTargetAdapter(ObjectProvider<LoadBalancerClient> loadBalancer) {
        return new HttpTargetAdapter(loadBalancer.getIfAvailable() != null);
    }

    @Bean
    @ConditionalOnMissingBean
    public StripPrefixFilterAdapter stripPrefixFilterAdapter() {
        return new StripPrefixFilterAdapter();
    }

    @Bean
    @ConditionalOnMissingBean
    public RewritePathFilterAdapter rewritePathFilterAdapter() {
        return new RewritePathFilterAdapter();
    }

    @Bean
    @ConditionalOnMissingBean
    public SetRequestHeaderFilterAdapter setRequestHeaderFilterAdapter() {
        return new SetRequestHeaderFilterAdapter();
    }

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerFilterAdapter circuitBreakerFilterAdapter(
            ObjectProvider<CircuitBreakerFactory<?, ?>> factories) {
        return new CircuitBreakerFilterAdapter(factories.getIfAvailable() != null);
    }
}
