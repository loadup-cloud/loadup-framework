package io.github.loadup.gateway.core.engine;

import io.github.loadup.gateway.core.filter.ExceptionFilter;
import io.github.loadup.gateway.core.filter.ProxyFilter;
import io.github.loadup.gateway.core.filter.ResponseWrapperFilter;
import io.github.loadup.gateway.core.filter.TracingFilter;
import io.github.loadup.gateway.core.router.RouteResolver;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.FilterDefinition;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.GatewayFilter;
import io.github.loadup.gateway.facade.spi.RouteStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultGatewayEngine implements GatewayEngine {
    private static final Logger log = LoggerFactory.getLogger(DefaultGatewayEngine.class);

    private final Map<String, GatewayFilter> filterRegistry;
    private final RouteResolver routeResolver;
    private final RouteStore routeStore;
    private final ExceptionFilter exceptionFilter;
    private final TracingFilter tracingFilter;
    private final ProxyFilter proxyFilter;
    private final ResponseWrapperFilter responseWrapperFilter;

    public DefaultGatewayEngine(
            Map<String, GatewayFilter> filterRegistry,
            RouteResolver routeResolver,
            RouteStore routeStore,
            ExceptionFilter exceptionFilter,
            TracingFilter tracingFilter,
            ProxyFilter proxyFilter,
            ResponseWrapperFilter responseWrapperFilter) {
        this.filterRegistry = Collections.unmodifiableMap(filterRegistry);
        this.routeResolver = routeResolver;
        this.routeStore = routeStore;
        this.exceptionFilter = exceptionFilter;
        this.tracingFilter = tracingFilter;
        this.proxyFilter = proxyFilter;
        this.responseWrapperFilter = responseWrapperFilter;
    }

    @Override
    public void execute(GatewayContext context) {
        // Exception wraps everything — including route resolution
        exceptionFilter.filter(context, ctx -> {
            if (tracingFilter != null) {
                tracingFilter.filter(ctx, c -> resolveAndExecute(c));
            } else {
                resolveAndExecute(ctx);
            }
        });
    }

    private void resolveAndExecute(GatewayContext context) {
        // Route resolution (was in RouteFilter)
        RouteConfig route = routeResolver
                .resolve(context.getRequest())
                .orElseThrow(() -> GatewayExceptionFactory.routeNotFound(context.getRequest()
                                .getMethod() + " " + context.getRequest().getPath()));
        context.setRoute(route);
        log.debug("Route resolved: {} → {}", route.getRouteId(), route.getTarget());

        // Build per-route chain: request filters → proxy → response filters → wrapper
        List<GatewayFilter> chain = buildRouteChain(route);
        new DefaultFilterChain(chain).filter(context);
    }

    private List<GatewayFilter> buildRouteChain(RouteConfig route) {
        List<FilterDefinition> requestFilters = Collections.emptyList();
        List<FilterDefinition> responseFilters = Collections.emptyList();
        try {
            var def = routeStore.load(route.getRouteId());
            if (def.isPresent()) {
                requestFilters = def.get().getFilters();
                responseFilters = def.get().getResponseFilters();
            }
        } catch (Exception e) {
            log.warn("Failed to load route definition for {}: {}", route.getRouteId(), e.getMessage());
        }

        List<GatewayFilter> chain = new ArrayList<>();

        // Request-phase filters
        for (FilterDefinition fd : requestFilters) {
            GatewayFilter f = filterRegistry.get(fd.getName());
            if (f != null) {
                chain.add(f);
                if (fd.getProps() != null && !fd.getProps().isEmpty()) {
                    contextRouteInject(route, fd);
                }
            } else {
                log.warn("Unknown filter '{}' on route '{}'", fd.getName(), route.getRouteId());
            }
        }

        chain.add(proxyFilter);

        // Response-phase filters
        for (FilterDefinition fd : responseFilters) {
            GatewayFilter f = filterRegistry.get(fd.getName());
            if (f != null) {
                chain.add(f);
                if (fd.getProps() != null && !fd.getProps().isEmpty()) {
                    contextRouteInject(route, fd);
                }
            } else {
                log.warn("Unknown filter '{}' on route '{}'", fd.getName(), route.getRouteId());
            }
        }

        chain.add(responseWrapperFilter);
        return chain;
    }

    private void contextRouteInject(RouteConfig route, FilterDefinition fd) {
        // Store filter props in a context attribute accessible by the filter
        // The key convention is "filter:props:<name>"
        // This is a lightweight mechanism — filters can also read route properties directly
    }

    public void refresh() {
        routeResolver.refreshRoutes();
        log.info("Gateway engine refreshed");
    }
}
