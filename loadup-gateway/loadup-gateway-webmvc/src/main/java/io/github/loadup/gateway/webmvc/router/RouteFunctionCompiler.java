package io.github.loadup.gateway.webmvc.router;

import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.webmvc.exception.GatewayExceptionHandler;
import io.github.loadup.gateway.webmvc.filter.CircuitBreakerHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.filter.RateLimitHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.filter.ResponseWrapperHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.filter.SecurityHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.filter.TracingHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.proxy.ProxyHandlerFunction;
import io.github.loadup.gateway.webmvc.support.GatewayAttributes;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.function.RequestPredicate;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Compiles compiled {@link RouteConfig}s into an executable {@link RouterFunction}.
 *
 * <p>Each route becomes a predicate + the shared proxy handler, wrapped with the fixed
 * pipeline filters. Per-route settings (security code, rate limit / circuit breaker
 * properties, response wrapping) are honored by the filters, so the pipeline is the same
 * for every route.
 *
 * <p>Filter order (outermost first): exception handler, tracing, security, rate limit,
 * circuit breaker, response wrapper, proxy handler.
 */
public final class RouteFunctionCompiler {
    private static final Logger log = LoggerFactory.getLogger(RouteFunctionCompiler.class);

    private RouteFunctionCompiler() {}

    /**
     * Compile a list of routes into a single combined router function. Disabled routes are
     * skipped. The returned function is immutable; callers publish it atomically.
     */
    public static RouterFunction<ServerResponse> compile(
            List<RouteConfig> routes,
            ProxyHandlerFunction proxyHandler,
            GatewayExceptionHandler exceptionHandler,
            TracingHandlerFilterFunction tracingFilter,
            SecurityHandlerFilterFunction securityFilter,
            RateLimitHandlerFilterFunction rateLimitFilter,
            CircuitBreakerHandlerFilterFunction circuitBreakerFilter,
            ResponseWrapperHandlerFilterFunction responseWrapperFilter) {

        RouterFunction<ServerResponse> result = request -> Optional.empty();
        int compiled = 0;
        for (RouteConfig route : routes) {
            if (!route.isEnabled()) {
                continue;
            }
            RouterFunction<ServerResponse> routeFunction = compileRoute(
                    route,
                    proxyHandler,
                    exceptionHandler,
                    tracingFilter,
                    securityFilter,
                    rateLimitFilter,
                    circuitBreakerFilter,
                    responseWrapperFilter);
            result = result.and(routeFunction);
            compiled++;
        }
        log.info("Compiled {} gateway routes", compiled);
        return result;
    }

    private static RouterFunction<ServerResponse> compileRoute(
            RouteConfig route,
            ProxyHandlerFunction proxyHandler,
            GatewayExceptionHandler exceptionHandler,
            TracingHandlerFilterFunction tracingFilter,
            SecurityHandlerFilterFunction securityFilter,
            RateLimitHandlerFilterFunction rateLimitFilter,
            CircuitBreakerHandlerFilterFunction circuitBreakerFilter,
            ResponseWrapperHandlerFilterFunction responseWrapperFilter) {

        RequestPredicate predicate = buildPredicate(route);
        RouterFunction<ServerResponse> routeFunction = RouterFunctions.route(predicate, proxyHandler);

        // Innermost first: the last filter applied runs first (outermost).
        routeFunction = routeFunction.filter(responseWrapperFilter);
        if (circuitBreakerFilter != null) {
            routeFunction = routeFunction.filter(circuitBreakerFilter);
        }
        if (rateLimitFilter != null) {
            routeFunction = routeFunction.filter(rateLimitFilter);
        }
        if (securityFilter != null) {
            routeFunction = routeFunction.filter(securityFilter);
        }
        if (tracingFilter != null) {
            routeFunction = routeFunction.filter(tracingFilter);
        }
        routeFunction = routeFunction.filter(exceptionHandler);
        return routeFunction;
    }

    /**
     * Builds a request predicate that matches the route method + path and records the
     * compiled {@link RouteConfig} on the request so downstream filters can access it.
     */
    private static RequestPredicate buildPredicate(RouteConfig route) {
        RequestPredicate methodPredicate =
                RequestPredicates.method(HttpMethod.valueOf(route.getMethod().toUpperCase(Locale.ROOT)));
        RequestPredicate pathPredicate = RequestPredicates.path(route.getPath());

        return new RequestPredicate() {
            @Override
            public boolean test(ServerRequest request) {
                if (methodPredicate.test(request) && pathPredicate.test(request)) {
                    MvcUtils.putAttribute(request, GatewayAttributes.ROUTE_CONFIG, route);
                    return true;
                }
                return false;
            }

            @Override
            public String toString() {
                return route.getMethod() + " " + route.getPath();
            }
        };
    }
}
