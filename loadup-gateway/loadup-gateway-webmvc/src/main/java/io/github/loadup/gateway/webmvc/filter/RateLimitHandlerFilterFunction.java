/*-
 * #%L
 * Loadup Gateway WebMVC Engine
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.gateway.webmvc.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.exception.ErrorType;
import io.github.loadup.gateway.facade.exception.GatewayException;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.webmvc.support.GatewayAttributes;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Per-(route, IP) token-bucket rate limiting backed by a bounded Caffeine cache.
 */
public class RateLimitHandlerFilterFunction implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    private static final Logger log = LoggerFactory.getLogger(RateLimitHandlerFilterFunction.class);

    private final Cache<String, TokenBucket> buckets;

    public RateLimitHandlerFilterFunction(GatewayProperties gatewayProperties) {
        this.buckets = Caffeine.newBuilder()
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

        RateLimitConfig config = parseConfig(route);
        if (!config.enabled) {
            return next.handle(request);
        }

        String limitKey = buildLimitKey(request, route, config.keySource);
        TokenBucket bucket = buckets.get(limitKey, k -> new TokenBucket(config.capacity, config.refillRate));
        if (!bucket.tryAcquire()) {
            log.warn("Rate limit exceeded: key={}, route={}", limitKey, route.getRouteId());
            throw new GatewayException(
                    "RATE_LIMIT_EXCEEDED",
                    ErrorType.RATE_LIMIT,
                    "RATE_LIMIT",
                    String.format(
                            "Rate limit exceeded (capacity=%d, refill=%.1f/s)", config.capacity, config.refillRate));
        }

        return next.handle(request);
    }

    private String buildLimitKey(ServerRequest request, RouteConfig route, String keySource) {
        String ip = request.remoteAddress()
                .map(a -> a.getAddress() != null ? a.getAddress().getHostAddress() : "unknown")
                .orElse("unknown");
        return switch (keySource) {
            case "IP" -> "ip:" + ip;
            case "ROUTE" -> "route:" + route.getRouteId();
            default -> route.getRouteId() + ":" + ip;
        };
    }

    private RateLimitConfig parseConfig(RouteConfig route) {
        Object enabled = route.getProperties().get("rateLimit.enabled");
        boolean isEnabled =
                enabled instanceof Boolean b ? b : (enabled instanceof String s ? Boolean.parseBoolean(s) : false);
        if (!isEnabled) {
            return new RateLimitConfig(false, 100L, 10.0, "COMBINED");
        }
        long capacity = parseLong(route.getProperties().get("rateLimit.capacity"), 100L);
        double refillRate = parseDouble(route.getProperties().get("rateLimit.refillRate"), 10.0);
        Object ks = route.getProperties().get("rateLimit.keySource");
        String keySource = ks instanceof String s ? s.toUpperCase() : "COMBINED";
        return new RateLimitConfig(true, capacity, refillRate, keySource);
    }

    private static long parseLong(Object v, long def) {
        if (v instanceof Number n) {
            return n.longValue();
        }
        if (v instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return def;
    }

    private static double parseDouble(Object v, double def) {
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        if (v instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return def;
    }

    private record RateLimitConfig(boolean enabled, long capacity, double refillRate, String keySource) {
        RateLimitConfig() {
            this(false, 100L, 10.0, "COMBINED");
        }
    }

    /**
     * Token bucket with refill in tokens per second.
     */
    public static class TokenBucket {
        private final long capacity;
        private final double refillRate;
        private final AtomicLong tokens;
        private final AtomicLong lastRefillNanos;

        public TokenBucket(long capacity, double refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = new AtomicLong(capacity * 1_000_000_000L);
            this.lastRefillNanos = new AtomicLong(System.nanoTime());
        }

        public boolean tryAcquire() {
            long required = 1_000_000_000L;
            while (true) {
                long cur = tokens.get();
                long now = System.nanoTime();
                long elapsed = now - lastRefillNanos.get();
                long refill = (long) (elapsed * refillRate);
                long updated = Math.min(capacity * 1_000_000_000L, cur + refill);
                if (updated < required) {
                    lastRefillNanos.set(now);
                    return false;
                }
                if (tokens.compareAndSet(cur, updated - required)) {
                    lastRefillNanos.set(now);
                    return true;
                }
            }
        }
    }
}
