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
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

/**
 * Token-bucket rate limiting action.
 *
 * <p>Enforces per-route or per-client rate limits using a lightweight
 * in-memory token-bucket algorithm. Each bucket has a capacity (max tokens)
 * and refill rate (tokens per second).
 *
 * <p>Rate limits are configured per-route via route properties:
 * <pre>
 *   rateLimit.enabled=true
 *   rateLimit.capacity=100       # max burst capacity
 *   rateLimit.refillRate=10      # tokens per second
 *   rateLimit.keySource=IP       # rate limit key: IP (per-client), ROUTE (per-route), or COMBINED (per-client-per-route)
 * </pre>
 *
 * <p>When the limit is exceeded, the action throws a GatewayException
 * with ErrorType.RATE_LIMIT and HTTP 429 status.
 */
@Slf4j
public class RateLimitAction implements GatewayAction {

    private final GatewayProperties gatewayProperties;
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitAction(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public void execute(GatewayContext context, GatewayActionChain chain) {
        RouteConfig route = context.getRoute();
        if (route == null) {
            chain.proceed(context);
            return;
        }

        // Check if rate limiting is enabled for this route
        RateLimitConfig config = parseRateLimitConfig(route, gatewayProperties);
        if (!config.enabled) {
            chain.proceed(context);
            return;
        }

        // Build rate limit key
        String limitKey = buildLimitKey(context.getRequest(), route, config.keySource);

        // Acquire token
        TokenBucket bucket = buckets.computeIfAbsent(
                limitKey, k -> new TokenBucket(config.capacity, config.refillRate));
        if (!bucket.tryAcquire()) {
            log.warn(
                    "Rate limit exceeded: key={}, route={}, client={}",
                    limitKey,
                    route.getRouteId(),
                    context.getRequest().getClientIp());
            throw rateLimitExceeded(route, config);
        }

        log.debug("Rate limit token acquired: key={}, available={}", limitKey, bucket.availableTokens());
        chain.proceed(context);
    }

    /**
     * Parse rate limit configuration from route properties with fallback to global defaults.
     */
    static RateLimitConfig parseRateLimitConfig(RouteConfig route, GatewayProperties props) {
        RateLimitConfig config = new RateLimitConfig();

        // Check route-level properties first
        Object enabledObj = route.getProperties().get("rateLimit.enabled");
        if (enabledObj instanceof Boolean b) {
            config.enabled = b;
        } else if (enabledObj instanceof String s) {
            config.enabled = Boolean.parseBoolean(s);
        } else {
            config.enabled = false; // Default: disabled unless explicitly enabled
        }

        if (!config.enabled) {
            return config;
        }

        config.capacity = parseLong(route.getProperties().get("rateLimit.capacity"), 100L);
        config.refillRate = parseDouble(route.getProperties().get("rateLimit.refillRate"), 10.0);

        Object keySourceObj = route.getProperties().get("rateLimit.keySource");
        if (keySourceObj instanceof String s) {
            config.keySource = s.toUpperCase();
        } else {
            config.keySource = "COMBINED"; // Default: per-client-per-route
        }

        return config;
    }

    private String buildLimitKey(GatewayRequest request, RouteConfig route, String keySource) {
        return switch (keySource) {
            case "IP" -> "ip:" + request.getClientIp();
            case "ROUTE" -> "route:" + route.getRouteId();
            case "COMBINED" -> route.getRouteId() + ":" + request.getClientIp();
            default -> route.getRouteId() + ":" + request.getClientIp();
        };
    }

    private GatewayException rateLimitExceeded(RouteConfig route, RateLimitConfig config) {
        return new GatewayException(
                "RATE_LIMIT_EXCEEDED",
                io.github.loadup.gateway.facade.exception.ErrorType.RATE_LIMIT,
                "RATE_LIMIT",
                String.format(
                        "Rate limit exceeded for route %s (capacity=%d, refillRate=%.1f/s)",
                        route.getRouteId(), config.capacity, config.refillRate));
    }

    private static long parseLong(Object value, long defaultValue) {
        if (value instanceof Number n) return n.longValue();
        if (value instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    private static double parseDouble(Object value, double defaultValue) {
        if (value instanceof Number n) return n.doubleValue();
        if (value instanceof String s) {
            try { return Double.parseDouble(s); } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    /**
     * Lightweight token-bucket implementation.
     *
     * <p>Thread-safe: uses AtomicLong for nanosecond-precision time tracking.
     * No external scheduler needed — tokens are refilled lazily on each acquire attempt.
     */
    static class TokenBucket {
        private final long capacity;
        private final double refillRate; // tokens per second
        private final AtomicLong tokens; // stored as nano-tokens (tokens * 1e9) for precision
        private final AtomicLong lastRefillNanos;

        TokenBucket(long capacity, double refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = new AtomicLong(capacity * 1_000_000_000L);
            this.lastRefillNanos = new AtomicLong(System.nanoTime());
        }

        boolean tryAcquire() {
            return tryAcquire(1);
        }

        boolean tryAcquire(long count) {
            long required = count * 1_000_000_000L;
            while (true) {
                long currentTokens = tokens.get();
                long now = System.nanoTime();
                long lastRefill = lastRefillNanos.get();

                // Calculate refill
                long elapsedNanos = now - lastRefill;
                long refillNanos =
                        (long) (elapsedNanos * refillRate); // refillRate tokens per second = refillRate nano-tokens per nanosecond
                long newTokens = Math.min(currentTokens + refillNanos, capacity * 1_000_000_000L);

                if (newTokens < required) {
                    return false;
                }

                if (tokens.compareAndSet(currentTokens, newTokens - required)) {
                    lastRefillNanos.set(now);
                    return true;
                }
            }
        }

        long availableTokens() {
            long now = System.nanoTime();
            long lastRefill = lastRefillNanos.get();
            long elapsedNanos = now - lastRefill;
            long refillNanos = (long) (elapsedNanos * refillRate);
            long current = tokens.get();
            return Math.min(current + refillNanos, capacity * 1_000_000_000L) / 1_000_000_000L;
        }
    }

    /**
     * Parsed rate limit configuration for a single route.
     */
    static class RateLimitConfig {
        boolean enabled = false;
        long capacity = 100;
        double refillRate = 10.0;
        String keySource = "COMBINED";
    }
}
