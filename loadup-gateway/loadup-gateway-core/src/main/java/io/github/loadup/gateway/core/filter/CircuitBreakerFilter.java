package io.github.loadup.gateway.core.filter;

/*-
 * #%L
 * LoadUp Gateway Core
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.FilterChain;
import io.github.loadup.gateway.facade.spi.GatewayFilter;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Circuit breaker filter — wraps the proxy chain.
 *
 * <p>Uses Caffeine with TTL eviction to prevent memory leaks.
 * CLOSED → OPEN → HALF_OPEN → CLOSED lifecycle.
 */
public class CircuitBreakerFilter implements GatewayFilter {
    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerFilter.class);

    private final GatewayProperties gatewayProperties;
    private final Clock clock;
    private final Cache<String, CircuitBreaker> breakers;

    public CircuitBreakerFilter(GatewayProperties gatewayProperties) {
        this(gatewayProperties, Clock.systemUTC());
    }

    CircuitBreakerFilter(GatewayProperties gatewayProperties, Clock clock) {
        this.gatewayProperties = gatewayProperties;
        this.clock = clock;
        this.breakers = Caffeine.newBuilder()
                .maximumSize(5_000)
                .expireAfterAccess(Duration.ofHours(1))
                .build();
    }

    @Override
    public String name() {
        return "circuit-breaker";
    }

    @Override
    public void filter(GatewayContext context, FilterChain chain) {
        RouteConfig route = context.getRoute();
        if (route == null) {
            chain.filter(context);
            return;
        }

        CBConfig config = parseConfig(route);
        if (!config.enabled) {
            chain.filter(context);
            return;
        }

        String key = breakerKey(route);
        CircuitBreaker breaker = breakers.get(key, k -> new CircuitBreaker(config, clock));
        if (breaker == null || !breaker.allowRequest()) {
            log.warn("Circuit OPEN for target={}, route={}", route.getTarget(), route.getRouteId());
            context.setResponse(buildShortCircuitResponse(context, route));
            return;
        }

        try {
            chain.filter(context);
            GatewayResponse resp = context.getResponse();
            if (resp != null && resp.getStatusCode() >= 500) {
                breaker.recordFailure();
            } else {
                breaker.recordSuccess();
            }
        } catch (Exception e) {
            breaker.recordFailure();
            throw e;
        }
    }

    private String breakerKey(RouteConfig route) {
        if (route.getTargetUrl() != null && !route.getTargetUrl().isEmpty()) return route.getTargetUrl();
        return route.getTarget();
    }

    private GatewayResponse buildShortCircuitResponse(GatewayContext context, RouteConfig route) {
        return GatewayResponse.builder()
                .requestId(context.getRequest().getRequestId())
                .statusCode(503)
                .body(
                        "{\"result\":{\"code\":\"CIRCUIT_OPEN\",\"status\":\"FAIL\",\"message\":\"Circuit breaker open\"},\"data\":null}")
                .contentType("application/json")
                .build();
    }

    static CBConfig parseConfig(RouteConfig route) {
        CBConfig c = new CBConfig();
        Object e = route.getProperties().get("circuitBreaker.enabled");
        c.enabled = e instanceof Boolean b ? b : (e instanceof String s ? Boolean.parseBoolean(s) : false);
        if (!c.enabled) return c;
        c.failureThreshold = parseInt(route, "circuitBreaker.failureThreshold", 5);
        c.openTimeoutSeconds = parseInt(route, "circuitBreaker.openTimeout", 30);
        c.halfOpenMax = parseInt(route, "circuitBreaker.halfOpenMax", 3);
        c.successThreshold = parseInt(route, "circuitBreaker.successThreshold", 2);
        return c;
    }

    private static int parseInt(RouteConfig r, String key, int def) {
        Object v = r.getProperties().get(key);
        if (v instanceof Number n) return n.intValue();
        if (v instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }

    static class CircuitBreaker {
        enum State {
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

        CircuitBreaker(CBConfig config, Clock clock) {
            this.config = config;
            this.clock = clock;
        }

        boolean allowRequest() {
            State s = state.get();
            if (s == State.CLOSED) return true;
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

        void recordSuccess() {
            failureCount.set(0);
            if (state.get() == State.HALF_OPEN && successCount.incrementAndGet() >= config.successThreshold) {
                if (state.compareAndSet(State.HALF_OPEN, State.CLOSED)) {
                    halfOpenRequests.set(0);
                    log.info("Circuit breaker → CLOSED");
                }
            }
        }

        void recordFailure() {
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

    static class CBConfig {
        boolean enabled;
        int failureThreshold = 5;
        int openTimeoutSeconds = 30;
        int halfOpenMax = 3;
        int successThreshold = 2;
    }
}
