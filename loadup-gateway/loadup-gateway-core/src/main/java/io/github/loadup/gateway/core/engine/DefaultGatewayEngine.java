package io.github.loadup.gateway.core.engine;

import io.github.loadup.gateway.core.filter.ExceptionFilter;
import io.github.loadup.gateway.core.filter.ProxyFilter;
import io.github.loadup.gateway.core.filter.ResponseWrapperFilter;
import io.github.loadup.gateway.core.filter.RouteFilter;
import io.github.loadup.gateway.core.filter.TracingFilter;
import io.github.loadup.gateway.core.router.RouteResolver;
import io.github.loadup.gateway.facade.context.GatewayContext;
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
/**
 * Default gateway engine — resolves routes and executes per-route filter chains.
 *
 * <p>Architecture:
 * <pre>
 *   ExceptionFilter → TracingFilter → RouteFilter → [dynamic route chain]
 *
 *   Dynamic route chain per RouteDefinition:
 *     [request filters...] → ProxyFilter → [response filters...] → ResponseWrapperFilter
 * </pre>
 *
 * <p>RouteFilter is the pivot: it resolves the route, then builds and executes
 * the route-specific sub-chain from the {@link RouteDefinition}.
 */
public class DefaultGatewayEngine implements GatewayEngine {
    private static final Logger log = LoggerFactory.getLogger(DefaultGatewayEngine.class);


    private final Map<String, GatewayFilter> filterRegistry;
    private final RouteResolver routeResolver;
    private final RouteStore routeStore;
    private final ExceptionFilter exceptionFilter;
    private final TracingFilter tracingFilter;
    private final RouteFilter routeFilter;
    private final ProxyFilter proxyFilter;
    private final ResponseWrapperFilter responseWrapperFilter;

    public DefaultGatewayEngine(
            Map<String, GatewayFilter> filterRegistry,
            RouteResolver routeResolver,
            RouteStore routeStore,
            ExceptionFilter exceptionFilter,
            TracingFilter tracingFilter,
            RouteFilter routeFilter,
            ProxyFilter proxyFilter,
            ResponseWrapperFilter responseWrapperFilter) {
        this.filterRegistry = Collections.unmodifiableMap(filterRegistry);
        this.routeResolver = routeResolver;
        this.routeStore = routeStore;
        this.exceptionFilter = exceptionFilter;
        this.tracingFilter = tracingFilter;
        this.routeFilter = routeFilter;
        this.proxyFilter = proxyFilter;
        this.responseWrapperFilter = responseWrapperFilter;
    }

    @Override
    public void execute(GatewayContext context) {
        exceptionFilter.filter(context, ctx -> {
            if (tracingFilter != null) {
                tracingFilter.filter(ctx, c -> routeFilter.filter(c, unused -> {}));
            } else {
                routeFilter.filter(ctx, unused -> {});
            }
        });
    }

    /**
     * Called by RouteFilter after route resolution to build and execute
     * the route-specific sub-chain.
     */
    public void executeRouteChain(GatewayContext context, RouteConfig route) {
        // Load RouteDefinition from RouteStore for per-route filter declarations
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

        // Resolve filter names to instances
        List<GatewayFilter> chain = new ArrayList<>();
        for (FilterDefinition fd : requestFilters) {
            GatewayFilter f = filterRegistry.get(fd.getName());
            if (f != null) {
                chain.add(f);
                // Inject per-filter properties into context for the filter to read
                if (fd.getProps() != null && !fd.getProps().isEmpty()) {
                    context.setAttribute("filter:props:" + fd.getName(), fd.getProps());
                }
            } else {
                log.warn("Unknown filter '{}' declared on route '{}'", fd.getName(), route.getRouteId());
            }
        }

        chain.add(proxyFilter);

        for (FilterDefinition fd : responseFilters) {
            GatewayFilter f = filterRegistry.get(fd.getName());
            if (f != null) {
                chain.add(f);
                if (fd.getProps() != null && !fd.getProps().isEmpty()) {
                    context.setAttribute("filter:props:" + fd.getName(), fd.getProps());
                }
            } else {
                log.warn("Unknown filter '{}' declared on route '{}'", fd.getName(), route.getRouteId());
            }
        }

        chain.add(responseWrapperFilter);

        new DefaultFilterChain(chain).filter(context);
    }

    /**
     * Refresh filter chain caches after route store changes.
     */
    public void refresh() {
        routeResolver.refreshRoutes();
        log.info("Gateway engine refreshed");
    }
}
