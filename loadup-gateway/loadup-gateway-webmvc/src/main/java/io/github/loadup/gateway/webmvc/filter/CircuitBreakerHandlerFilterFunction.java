package io.github.loadup.gateway.webmvc.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.webmvc.support.GatewayAttributes;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Per-route circuit breaker (CLOSED → OPEN → HALF_OPEN) backed by a bounded Caffeine cache.
 */
public class CircuitBreakerHandlerFilterFunction implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerHandlerFilterFunction.class);

    private static final String SHORT_CIRCUIT_BODY =
            "{\"result\":{\"code\":\"CIRCUIT_OPEN\",\"status\":\"FAIL\",\"message\":\"Circuit breaker open\"},\"data\":null}";

    private final Cache<String, CircuitBreaker> breakers;
    private final Clock clock;

    public CircuitBreakerHandlerFilterFunction() {
        this(Clock.systemDefaultZone());
    }

    public CircuitBreakerHandlerFilterFunction(Clock clock) {
        this.clock = clock;
        this.breakers = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(Duration.ofMinutes(30))
                .build();
    }

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next) throws Exception {
        RouteConfig route = MvcUtils.getAttribute(request, GatewayAttributes.ROUTE_CONFIG);
        if (route == null) {
            return next.handle(request);
        }

        CBConfig config = parseConfig(route);
        if (!config.enabled) {
            return next.handle(request);
        }

        String key = breakerKey(route);
        CircuitBreaker breaker = breakers.get(key, k -> new CircuitBreaker(config, clock));
        if (!breaker.allowRequest()) {
            log.warn("Circuit breaker OPEN, short-circuiting route '{}'", route.getRouteId());
            return ServerResponse.status(503)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(SHORT_CIRCUIT_BODY);
        }

        try {
            ServerResponse response = next.handle(request);
            if (response.statusCode().value() >= 500) {
                breaker.recordFailure();
            } else {
                breaker.recordSuccess();
            }
            return response;
        } catch (Exception e) {
            breaker.recordFailure();
            throw e;
        }
    }

    private String breakerKey(RouteConfig route) {
        if (route.getTargetUrl() != null && !route.getTargetUrl().isEmpty()) {
            return route.getTargetUrl();
        }
        return route.getTarget();
    }

    private CBConfig parseConfig(RouteConfig route) {
        CBConfig c = new CBConfig();
        Object e = route.getProperties().get("circuitBreaker.enabled");
        c.enabled = e instanceof Boolean b ? b : (e instanceof String s ? Boolean.parseBoolean(s) : false);
        if (!c.enabled) {
            return c;
        }
        c.failureThreshold = parseInt(route, "circuitBreaker.failureThreshold", 5);
        c.openTimeoutSeconds = parseInt(route, "circuitBreaker.openTimeout", 30);
        c.halfOpenMax = parseInt(route, "circuitBreaker.halfOpenMax", 3);
        c.successThreshold = parseInt(route, "circuitBreaker.successThreshold", 2);
        return c;
    }

    private static int parseInt(RouteConfig r, String key, int def) {
        Object v = r.getProperties().get(key);
        if (v instanceof Number n) {
            return n.intValue();
        }
        if (v instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return def;
    }

    /**
     * Per-route circuit breaker state machine.
     */
    public static class CircuitBreaker {
        public enum State {
            CLOSED,
            OPEN,
            HALF_OPEN
        }

        private final CBConfig config;
        private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger halfOpenRequests = new AtomicInteger(0);
        private final AtomicLong openedAtMillis = new AtomicLong(0);
        private final Clock clock;

        public CircuitBreaker(CBConfig config, Clock clock) {
            this.config = config;
            this.clock = clock;
        }

        public boolean allowRequest() {
            State s = state.get();
            if (s == State.CLOSED) {
                return true;
            }
            if (s == State.OPEN) {
                if (clock.millis() - openedAtMillis.get() >= config.openTimeoutSeconds * 1000L) {
                    if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                        halfOpenRequests.set(0);
                        successCount.set(0);
                        log.info("Circuit breaker → HALF_OPEN");
                    }
                    return allowRequest();
                }
                return false;
            }
            return halfOpenRequests.incrementAndGet() <= config.halfOpenMax;
        }

        public void recordSuccess() {
            failureCount.set(0);
            if (state.get() == State.HALF_OPEN && successCount.incrementAndGet() >= config.successThreshold) {
                if (state.compareAndSet(State.HALF_OPEN, State.CLOSED)) {
                    halfOpenRequests.set(0);
                    log.info("Circuit breaker → CLOSED");
                }
            }
        }

        public void recordFailure() {
            if (state.get() == State.CLOSED && failureCount.incrementAndGet() >= config.failureThreshold) {
                if (state.compareAndSet(State.CLOSED, State.OPEN)) {
                    openedAtMillis.set(clock.millis());
                    log.warn("Circuit breaker → OPEN after {} failures", failureCount.get());
                }
            } else if (state.get() == State.HALF_OPEN) {
                if (state.compareAndSet(State.HALF_OPEN, State.OPEN)) {
                    openedAtMillis.set(clock.millis());
                    failureCount.set(0);
                    halfOpenRequests.set(0);
                    log.warn("Circuit breaker → RE-OPENED");
                }
            }
        }
    }

    public static class CBConfig {
        boolean enabled;
        int failureThreshold = 5;
        int openTimeoutSeconds = 30;
        int halfOpenMax = 3;
        int successThreshold = 2;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getFailureThreshold() {
            return failureThreshold;
        }

        public void setFailureThreshold(int failureThreshold) {
            this.failureThreshold = failureThreshold;
        }

        public int getOpenTimeoutSeconds() {
            return openTimeoutSeconds;
        }

        public void setOpenTimeoutSeconds(int openTimeoutSeconds) {
            this.openTimeoutSeconds = openTimeoutSeconds;
        }

        public int getHalfOpenMax() {
            return halfOpenMax;
        }

        public void setHalfOpenMax(int halfOpenMax) {
            this.halfOpenMax = halfOpenMax;
        }

        public int getSuccessThreshold() {
            return successThreshold;
        }

        public void setSuccessThreshold(int successThreshold) {
            this.successThreshold = successThreshold;
        }
    }
}
