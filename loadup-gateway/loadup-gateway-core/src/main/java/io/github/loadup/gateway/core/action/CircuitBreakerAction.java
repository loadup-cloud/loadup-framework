package io.github.loadup.gateway.core.action;

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

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayException;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

/**
 * Circuit breaker action for proxy call fault tolerance.
 *
 * <p>Wraps the proxy action with a circuit breaker pattern that prevents
 * cascading failures when a backend service is unhealthy. Each backend
 * target gets its own circuit breaker instance.
 *
 * <p>States: CLOSED → OPEN → HALF_OPEN → CLOSED
 *
 * <p>Configuration per route via properties or global defaults:
 * <pre>
 *   circuitBreaker.enabled=true
 *   circuitBreaker.failureThreshold=5     # consecutive failures to open circuit
 *   circuitBreaker.openTimeout=30          # seconds to stay open before half-open
 *   circuitBreaker.halfOpenMax=3           # max requests in half-open state
 *   circuitBreaker.successThreshold=2      # consecutive successes to close circuit
 * </pre>
 *
 * <p>When the circuit is OPEN, requests are short-circuited with a
 * 503 Service Unavailable response rather than attempting the backend call.
 */
@Slf4j
public class CircuitBreakerAction implements GatewayAction {

    private final GatewayProperties gatewayProperties;
    private final Clock clock;
    private final ConcurrentHashMap<String, CircuitBreaker> breakers = new ConcurrentHashMap<>();

    public CircuitBreakerAction(GatewayProperties gatewayProperties) {
        this(gatewayProperties, Clock.systemUTC());
    }

    CircuitBreakerAction(GatewayProperties gatewayProperties, Clock clock) {
        this.gatewayProperties = gatewayProperties;
        this.clock = clock;
    }

    @Override
    public void execute(GatewayContext context, GatewayActionChain chain) {
        RouteConfig route = context.getRoute();
        if (route == null) {
            chain.proceed(context);
            return;
        }

        CircuitBreakerConfig config = parseConfig(route, gatewayProperties);
        if (!config.enabled) {
            chain.proceed(context);
            return;
        }

        String breakerKey = buildBreakerKey(route);
        CircuitBreaker breaker = breakers.computeIfAbsent(breakerKey, k -> new CircuitBreaker(config));

        // Check if circuit allows the request
        if (!breaker.allowRequest()) {
            log.warn(
                    "Circuit OPEN for target={}, route={} — short-circuiting request",
                    route.getTarget(),
                    route.getRouteId());
            context.setResponse(buildShortCircuitResponse(context, route, breaker));
            return; // Don't proceed — return immediately with 503
        }

        // Allow request through — track outcome
        try {
            chain.proceed(context);

            // Record success or failure based on response status
            GatewayResponse response = context.getResponse();
            if (response != null && isServerError(response.getStatusCode())) {
                breaker.recordFailure();
                log.debug(
                        "Circuit breaker recorded failure: target={}, failures={}",
                        breakerKey,
                        breaker.getFailureCount());
            } else {
                breaker.recordSuccess();
            }
        } catch (Exception e) {
            breaker.recordFailure();
            log.debug(
                    "Circuit breaker recorded failure from exception: target={}, error={}",
                    breakerKey,
                    e.getMessage());
            throw e; // Let ExceptionAction handle it
        }
    }

    private boolean isServerError(int statusCode) {
        return statusCode >= 500;
    }

    private String buildBreakerKey(RouteConfig route) {
        // Group by target URL to share breaker across routes hitting same backend
        if (route.getTargetUrl() != null && !route.getTargetUrl().isEmpty()) {
            return route.getTargetUrl();
        }
        return route.getTarget();
    }

    private GatewayResponse buildShortCircuitResponse(
            GatewayContext context, RouteConfig route, CircuitBreaker breaker) {
        return GatewayResponse.builder()
                .requestId(context.getRequest().getRequestId())
                .statusCode(503)
                .body(String.format(
                        "{\"result\":{\"code\":\"CIRCUIT_OPEN\",\"status\":\"FAIL\","
                                + "\"message\":\"Circuit breaker is OPEN for target %s\"},"
                                + "\"data\":null,\"meta\":{\"requestId\":\"%s\"}}",
                        route.getTarget(), context.getRequest().getRequestId()))
                .contentType("application/json")
                .errorMessage("Circuit breaker open for target: " + route.getTarget())
                .build();
    }

    /**
     * Reset all circuit breakers (useful for testing or admin operations).
     */
    public void resetAll() {
        breakers.values().forEach(CircuitBreaker::reset);
        log.info("All circuit breakers reset");
    }

    /**
     * Reset circuit breaker for a specific target.
     */
    public void reset(String breakerKey) {
        CircuitBreaker breaker = breakers.get(breakerKey);
        if (breaker != null) {
            breaker.reset();
            log.info("Circuit breaker reset for target: {}", breakerKey);
        }
    }

    static CircuitBreakerConfig parseConfig(RouteConfig route, GatewayProperties props) {
        CircuitBreakerConfig config = new CircuitBreakerConfig();

        Object enabledObj = route.getProperties().get("circuitBreaker.enabled");
        if (enabledObj instanceof Boolean b) {
            config.enabled = b;
        } else if (enabledObj instanceof String s) {
            config.enabled = Boolean.parseBoolean(s);
        } else {
            config.enabled = false;
        }
        if (!config.enabled) return config;

        config.failureThreshold = parseIntProp(route, "circuitBreaker.failureThreshold", 5);
        config.openTimeoutSeconds = parseIntProp(route, "circuitBreaker.openTimeout", 30);
        config.halfOpenMaxRequests = parseIntProp(route, "circuitBreaker.halfOpenMax", 3);
        config.successThreshold = parseIntProp(route, "circuitBreaker.successThreshold", 2);

        return config;
    }

    private static int parseIntProp(RouteConfig route, String key, int defaultVal) {
        Object val = route.getProperties().get(key);
        if (val instanceof Number n) return n.intValue();
        if (val instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException ignored) {}
        }
        return defaultVal;
    }

    /**
     * Lightweight circuit breaker implementation.
     *
     * <p>States:
     * <ul>
     *   <li>CLOSED — normal operation, counting failures</li>
     *   <li>OPEN — failure threshold reached, short-circuit all requests</li>
     *   <li>HALF_OPEN — open timeout expired, allow limited probe requests</li>
     * </ul>
     */
    static class CircuitBreaker {
        private enum State { CLOSED, OPEN, HALF_OPEN }

        private final CircuitBreakerConfig config;
        private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger halfOpenRequests = new AtomicInteger(0);
        private final AtomicLong openedAtMillis = new AtomicLong(0);
        private final Clock clock;

        CircuitBreaker(CircuitBreakerConfig config) {
            this(config, Clock.systemUTC());
        }

        CircuitBreaker(CircuitBreakerConfig config, Clock clock) {
            this.config = config;
            this.clock = clock;
        }

        boolean allowRequest() {
            State current = state.get();

            if (current == State.CLOSED) {
                return true;
            }

            if (current == State.OPEN) {
                long openDuration = clock.millis() - openedAtMillis.get();
                if (openDuration >= config.openTimeoutSeconds * 1000L) {
                    // Transition to HALF_OPEN
                    if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                        halfOpenRequests.set(0);
                        successCount.set(0);
                        log.info("Circuit breaker transitioning to HALF_OPEN");
                    }
                    // Whatever state we're in now, proceed to the check below
                    return allowRequest();
                }
                return false; // Still OPEN
            }

            if (current == State.HALF_OPEN) {
                int currentRequests = halfOpenRequests.incrementAndGet();
                if (currentRequests <= config.halfOpenMaxRequests) {
                    return true;
                }
                // Exceeded half-open max — reject
                halfOpenRequests.decrementAndGet();
                return false;
            }

            return false;
        }

        void recordSuccess() {
            State current = state.get();
            failureCount.set(0); // Reset failure count on any success

            if (current == State.HALF_OPEN) {
                int successes = successCount.incrementAndGet();
                if (successes >= config.successThreshold) {
                    if (state.compareAndSet(State.HALF_OPEN, State.CLOSED)) {
                        halfOpenRequests.set(0);
                        log.info("Circuit breaker CLOSED — backend healthy again");
                    }
                }
            }
        }

        void recordFailure() {
            State current = state.get();

            if (current == State.CLOSED) {
                int failures = failureCount.incrementAndGet();
                if (failures >= config.failureThreshold) {
                    if (state.compareAndSet(State.CLOSED, State.OPEN)) {
                        openedAtMillis.set(clock.millis());
                        log.warn(
                                "Circuit breaker OPEN after {} consecutive failures",
                                failures);
                    }
                }
            } else if (current == State.HALF_OPEN) {
                // A failure in half-open re-opens the circuit immediately
                if (state.compareAndSet(State.HALF_OPEN, State.OPEN)) {
                    openedAtMillis.set(clock.millis());
                    failureCount.set(0);
                    halfOpenRequests.set(0);
                    log.warn("Circuit breaker RE-OPENED after failure in HALF_OPEN state");
                }
            }
        }

        void reset() {
            state.set(State.CLOSED);
            failureCount.set(0);
            successCount.set(0);
            halfOpenRequests.set(0);
            openedAtMillis.set(0);
        }

        int getFailureCount() {
            return failureCount.get();
        }

        State getState() {
            return state.get();
        }
    }

    static class CircuitBreakerConfig {
        boolean enabled = false;
        int failureThreshold = 5;
        int openTimeoutSeconds = 30;
        int halfOpenMaxRequests = 3;
        int successThreshold = 2;
    }
}
