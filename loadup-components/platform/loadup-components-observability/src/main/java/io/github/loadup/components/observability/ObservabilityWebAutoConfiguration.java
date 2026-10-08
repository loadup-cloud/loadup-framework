package io.github.loadup.components.observability;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/** Runs after Boot's server observation filter and before the security filter chain. */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "jakarta.servlet.Filter")
public class ObservabilityWebAutoConfiguration {
    @Bean
    public FilterRegistrationBean<TraceResponseHeaderFilter> traceResponseHeaderFilter(Tracer tracer) {
        FilterRegistrationBean<TraceResponseHeaderFilter> registration =
                new FilterRegistrationBean<>(new TraceResponseHeaderFilter(tracer));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        return registration;
    }
}
