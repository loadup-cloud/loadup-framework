package io.github.loadup.gateway.webmvc.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.loadup.components.resilience4j.ResilienceRegistries;
import io.github.loadup.gateway.facade.exception.ErrorType;
import io.github.loadup.gateway.facade.exception.GatewayException;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.webmvc.support.GatewayAttributes;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Per-(route, IP) rate limiting backed by a Resilience4j {@link RateLimiter}.
 *
 * <p>Rate limiter instances are kept in a bounded Caffeine cache so high-cardinality client IPs
 * cannot grow memory unboundedly. The route-level token bucket (capacity + refill per second) is
 * mapped to Resilience4j's {@code limit-for-period / limit-refresh-period} pair.
 */
public class RateLimitHandlerFilterFunction implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    private static final Logger log = LoggerFactory.getLogger(RateLimitHandlerFilterFunction.class);

    private static final long MAX_CAPACITY = Integer.MAX_VALUE;

    private final Cache<String, RateLimiter> limiters;

    public RateLimitHandlerFilterFunction(ResilienceRegistries registries) {
        this.limiters = Caffeine.newBuilder()
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

        RateLimitConfig config = RateLimitConfig.parse(route);
        if (!config.enabled()) {
            return next.handle(request);
        }

        String limitKey = buildLimitKey(request, route, config.keySource());
        RateLimiter limiter = limiters.get(limitKey, key -> RateLimiter.of(key, config.toRateLimiterConfig()));
        if (!limiter.acquirePermission()) {
            log.warn("Rate limit exceeded: key={}, route={}", limitKey, route.getRouteId());
            throw new GatewayException(
                    "RATE_LIMIT_EXCEEDED",
                    ErrorType.RATE_LIMIT,
                    "RATE_LIMIT",
                    String.format(
                            "Rate limit exceeded (capacity=%d, refill=%.1f/s)",
                            config.capacity(), config.refillRate()));
        }

        return next.handle(request);
    }

    private String buildLimitKey(ServerRequest request, RouteConfig route, String keySource) {
        String ip = request.remoteAddress()
                .map(address ->
                        address.getAddress() != null ? address.getAddress().getHostAddress() : "unknown")
                .orElse("unknown");
        return switch (keySource) {
            case "IP" -> "ip:" + ip;
            case "ROUTE" -> "route:" + route.getRouteId();
            default -> route.getRouteId() + ":" + ip;
        };
    }

    private record RateLimitConfig(boolean enabled, long capacity, double refillRate, String keySource) {

        static RateLimitConfig parse(RouteConfig route) {
            Object enabledValue = route.getProperties().get("rateLimit.enabled");
            boolean enabled = enabledValue instanceof Boolean b
                    ? b
                    : (enabledValue instanceof String s ? Boolean.parseBoolean(s) : false);
            if (!enabled) {
                return new RateLimitConfig(false, 100L, 10.0, "COMBINED");
            }
            long capacity = parseLong(route.getProperties().get("rateLimit.capacity"), 100L);
            double refillRate = parseDouble(route.getProperties().get("rateLimit.refillRate"), 10.0);
            Object keySourceValue = route.getProperties().get("rateLimit.keySource");
            String keySource = keySourceValue instanceof String s ? s.toUpperCase() : "COMBINED";
            return new RateLimitConfig(true, capacity, refillRate, keySource);
        }

        RateLimiterConfig toRateLimiterConfig() {
            long capacity = Math.min(Math.max(this.capacity, 1L), MAX_CAPACITY);
            double refillRate = this.refillRate <= 0 ? 10.0 : this.refillRate;
            long refreshPeriodSeconds = Math.max(1L, (long) Math.ceil(capacity / refillRate));
            return RateLimiterConfig.custom()
                    .limitForPeriod((int) capacity)
                    .limitRefreshPeriod(Duration.ofSeconds(refreshPeriodSeconds))
                    .timeoutDuration(Duration.ZERO)
                    .build();
        }

        private static long parseLong(Object value, long defaultValue) {
            if (value instanceof Number number) {
                return number.longValue();
            }
            if (value instanceof String string) {
                try {
                    return Long.parseLong(string);
                } catch (NumberFormatException ignored) {
                    // fall through
                }
            }
            return defaultValue;
        }

        private static double parseDouble(Object value, double defaultValue) {
            if (value instanceof Number number) {
                return number.doubleValue();
            }
            if (value instanceof String string) {
                try {
                    return Double.parseDouble(string);
                } catch (NumberFormatException ignored) {
                    // fall through
                }
            }
            return defaultValue;
        }
    }
}
