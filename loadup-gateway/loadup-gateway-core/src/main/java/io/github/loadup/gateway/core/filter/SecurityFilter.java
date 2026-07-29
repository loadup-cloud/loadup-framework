package io.github.loadup.gateway.core.filter;

import io.github.loadup.gateway.core.security.SecurityStrategyManager;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.FilterChain;
import io.github.loadup.gateway.facade.spi.GatewayFilter;
import io.github.loadup.gateway.facade.spi.SecurityStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Security filter — delegates to a named {@link SecurityStrategy} based on
 * the route's {@code securityCode}.
 *
 * <p>The filter is always registered; it reads the security code from the
 * resolved route at runtime. If the code is {@code OFF} or blank, it skips.
 */
@Slf4j
@RequiredArgsConstructor
public class SecurityFilter implements GatewayFilter {

    private final SecurityStrategyManager strategyManager;

    @Override
    public String name() {
        return "security";
    }

    @Override
    public void filter(GatewayContext context, FilterChain chain) {
        RouteConfig route = context.getRoute();
        if (route == null) {
            chain.filter(context);
            return;
        }

        String code = route.getSecurityCode();
        if (StringUtils.isBlank(code) || "OFF".equalsIgnoreCase(code)) {
            chain.filter(context);
            return;
        }

        SecurityStrategy strategy = strategyManager.getStrategy(code);
        if (strategy == null) {
            log.error("Unknown security code '{}' for route '{}'", code, route.getRouteId());
            throw GatewayExceptionFactory.systemError("Unknown security strategy: " + code);
        }

        try {
            strategy.process(context);
        } catch (Exception e) {
            log.warn("Security check failed: requestId={}", context.getRequest().getRequestId(), e);
            throw e;
        }

        chain.filter(context);
    }
}
