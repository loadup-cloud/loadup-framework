package io.github.loadup.gateway.webmvc.filter;

import io.github.loadup.components.resilience4j.ResilienceRegistries;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.webmvc.support.GatewayAttributes;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Per-route circuit breaker (CLOSED → OPEN → HALF_OPEN) backed by a Resilience4j
 * {@link CircuitBreakerRegistry}.
 *
 * <p>Routes pointing to the same upstream share one breaker instance. Stale instances are
 * pruned whenever the route snapshot is refreshed.
 */
public class CircuitBreakerHandlerFilterFunction implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerHandlerFilterFunction.class);

    private static final String INSTANCE_PREFIX = "gateway:";
    private static final String SHORT_CIRCUIT_BODY =
            "{\"result\":{\"code\":\"CIRCUIT_OPEN\",\"status\":\"FAIL\",\"message\":\"Circuit breaker open\"},\"data\":null}";

    private final CircuitBreakerRegistry registry;
    private final Set<String> activeKeys = ConcurrentHashMap.newKeySet();

    public CircuitBreakerHandlerFilterFunction(ResilienceRegistries registries) {
        this.registry = registries.circuitBreakerRegistry();
    }

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next) throws Exception {
        RouteConfig route = MvcUtils.getAttribute(request, GatewayAttributes.ROUTE_CONFIG);
        if (route == null) {
            return next.handle(request);
        }

        CBConfig config = CBConfig.parse(route);
        if (!config.enabled()) {
            return next.handle(request);
        }

        String key = keyOf(route);
        CircuitBreaker breaker = registry.circuitBreaker(INSTANCE_PREFIX + key, config.toCircuitBreakerConfig());
        activeKeys.add(key);

        if (!breaker.tryAcquirePermission()) {
            log.warn("Circuit breaker OPEN, short-circuiting route '{}'", route.getRouteId());
            return ServerResponse.status(503)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(SHORT_CIRCUIT_BODY);
        }

        long startNanos = System.nanoTime();
        try {
            ServerResponse response = next.handle(request);
            if (response.statusCode().value() >= 500) {
                breaker.onError(
                        System.nanoTime() - startNanos,
                        TimeUnit.NANOSECONDS,
                        new IllegalStateException(
                                "HTTP " + response.statusCode().value()));
            } else {
                breaker.onSuccess(System.nanoTime() - startNanos, TimeUnit.NANOSECONDS);
            }
            return response;
        } catch (Exception e) {
            breaker.onError(System.nanoTime() - startNanos, TimeUnit.NANOSECONDS, e);
            throw e;
        }
    }

    /**
     * Removes breaker instances whose route key is no longer active.
     */
    public void prune(Set<String> activeRouteKeys) {
        for (String key : activeKeys) {
            if (!activeRouteKeys.contains(key)) {
                registry.remove(INSTANCE_PREFIX + key);
                activeKeys.remove(key);
            }
        }
    }

    public static String keyOf(RouteConfig route) {
        if (route.getTargetUrl() != null && !route.getTargetUrl().isEmpty()) {
            return route.getTargetUrl();
        }
        return route.getTarget();
    }

    private record CBConfig(
            boolean enabled,
            float failureRateThreshold,
            int slidingWindowSize,
            int minimumNumberOfCalls,
            Duration waitDurationInOpenState,
            int permittedNumberOfCallsInHalfOpenState) {

        static CBConfig parse(RouteConfig route) {
            Object enabledValue = route.getProperties().get("circuitBreaker.enabled");
            boolean enabled = enabledValue instanceof Boolean b
                    ? b
                    : (enabledValue instanceof String s ? Boolean.parseBoolean(s) : false);
            if (!enabled) {
                return new CBConfig(false, 50, 10, 5, Duration.ofSeconds(30), 3);
            }
            float failureRateThreshold = parseFloat(route, "circuitBreaker.failureRateThreshold", 50.0f);
            int slidingWindowSize = parseInt(route, "circuitBreaker.slidingWindowSize", 10);
            int minimumNumberOfCalls = parseInt(
                    route,
                    "circuitBreaker.minimumNumberOfCalls",
                    parseInt(route, "circuitBreaker.failureThreshold", 5));
            Duration waitDurationInOpenState = Duration.ofSeconds(parseInt(
                    route,
                    "circuitBreaker.waitDurationInOpenState",
                    parseInt(route, "circuitBreaker.openTimeout", 30)));
            int permittedNumberOfCallsInHalfOpenState = parseInt(
                    route,
                    "circuitBreaker.permittedNumberOfCallsInHalfOpenState",
                    parseInt(route, "circuitBreaker.halfOpenMax", 3));
            return new CBConfig(
                    true,
                    failureRateThreshold,
                    slidingWindowSize,
                    minimumNumberOfCalls,
                    waitDurationInOpenState,
                    permittedNumberOfCallsInHalfOpenState);
        }

        CircuitBreakerConfig toCircuitBreakerConfig() {
            return CircuitBreakerConfig.custom()
                    .failureRateThreshold(failureRateThreshold)
                    .slidingWindowSize(slidingWindowSize)
                    .minimumNumberOfCalls(minimumNumberOfCalls)
                    .waitDurationInOpenState(waitDurationInOpenState)
                    .permittedNumberOfCallsInHalfOpenState(permittedNumberOfCallsInHalfOpenState)
                    .build();
        }

        private static int parseInt(RouteConfig route, String key, int defaultValue) {
            Object value = route.getProperties().get(key);
            if (value instanceof Number number) {
                return number.intValue();
            }
            if (value instanceof String string) {
                try {
                    return Integer.parseInt(string);
                } catch (NumberFormatException ignored) {
                    // fall through
                }
            }
            return defaultValue;
        }

        private static float parseFloat(RouteConfig route, String key, float defaultValue) {
            Object value = route.getProperties().get(key);
            if (value instanceof Number number) {
                return number.floatValue();
            }
            if (value instanceof String string) {
                try {
                    return Float.parseFloat(string);
                } catch (NumberFormatException ignored) {
                    // fall through
                }
            }
            return defaultValue;
        }
    }
}
