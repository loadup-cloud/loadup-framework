package io.github.loadup.gateway.core.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.ErrorType;
import io.github.loadup.gateway.facade.exception.GatewayException;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.FilterChain;
import io.github.loadup.gateway.facade.spi.GatewayFilter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Token-bucket rate limiting filter.
 *
 * <p>Uses Caffeine with size + TTL eviction instead of unbounded ConcurrentHashMap
 * to prevent memory leaks from stale (route, IP) entries.
 *
 * <p>Per-route config via filter props or route properties:
 * <pre>
 *   rateLimit.enabled=true
 *   rateLimit.capacity=100
 *   rateLimit.refillRate=10
 *   rateLimit.keySource=COMBINED  (IP | ROUTE | COMBINED)
 * </pre>
 */
public class RateLimitFilter implements GatewayFilter {
    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);


    private final GatewayProperties gatewayProperties;
    private final Cache<String, TokenBucket> buckets;

    public RateLimitFilter(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
        this.buckets = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(Duration.ofMinutes(30))
                .build();
    }

    @Override
    public String name() {
        return "rate-limit";
    }

    @Override
    public void filter(GatewayContext context, FilterChain chain) {
        RouteConfig route = context.getRoute();
        if (route == null) {
            chain.filter(context);
            return;
        }

        RateLimitConfig config = parseConfig(route);
        if (!config.enabled) {
            chain.filter(context);
            return;
        }

        String limitKey = buildLimitKey(context, route, config.keySource);
        TokenBucket bucket = buckets.get(limitKey, k -> new TokenBucket(config.capacity, config.refillRate));
        if (bucket == null || !bucket.tryAcquire()) {
            log.warn("Rate limit exceeded: key={}, route={}", limitKey, route.getRouteId());
            throw new GatewayException("RATE_LIMIT_EXCEEDED", ErrorType.RATE_LIMIT, "RATE_LIMIT",
                    String.format("Rate limit exceeded (capacity=%d, refill=%.1f/s)", config.capacity, config.refillRate));
        }

        chain.filter(context);
    }

    private String buildLimitKey(GatewayContext ctx, RouteConfig route, String keySource) {
        String ip = ctx.getRequest().getClientIp();
        return switch (keySource) {
            case "IP" -> "ip:" + ip;
            case "ROUTE" -> "route:" + route.getRouteId();
            default -> route.getRouteId() + ":" + ip;
        };
    }

    private RateLimitConfig parseConfig(RouteConfig route) {
        RateLimitConfig c = new RateLimitConfig();
        Object enabled = route.getProperties().get("rateLimit.enabled");
        c.enabled = enabled instanceof Boolean b ? b : (enabled instanceof String s ? Boolean.parseBoolean(s) : false);
        if (!c.enabled) return c;
        c.capacity = parseLong(route.getProperties().get("rateLimit.capacity"), 100L);
        c.refillRate = parseDouble(route.getProperties().get("rateLimit.refillRate"), 10.0);
        Object ks = route.getProperties().get("rateLimit.keySource");
        c.keySource = ks instanceof String s ? s.toUpperCase() : "COMBINED";
        return c;
    }

    private static long parseLong(Object v, long def) {
        if (v instanceof Number n) return n.longValue();
        if (v instanceof String s) { try { return Long.parseLong(s); } catch (NumberFormatException ignored) {} }
        return def;
    }

    private static double parseDouble(Object v, double def) {
        if (v instanceof Number n) return n.doubleValue();
        if (v instanceof String s) { try { return Double.parseDouble(s); } catch (NumberFormatException ignored) {} }
        return def;
    }

    static class TokenBucket {
        private final long capacity;
        private final double refillRate;
        private final AtomicLong tokens;
        private final AtomicLong lastRefillNanos;
        TokenBucket(long capacity, double refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = new AtomicLong(capacity * 1_000_000_000L);
            this.lastRefillNanos = new AtomicLong(System.nanoTime());
        }
        boolean tryAcquire() {
            long required = 1_000_000_000L;
            while (true) {
                long cur = tokens.get();
                long now = System.nanoTime();
                long elapsed = now - lastRefillNanos.get();
                long refill = (long) (elapsed * refillRate);
                long newTokens = Math.min(cur + refill, capacity * 1_000_000_000L);
                if (newTokens < required) return false;
                if (tokens.compareAndSet(cur, newTokens - required)) {
                    lastRefillNanos.set(now);
                    return true;
                }
            }
        }
    }

    static class RateLimitConfig {
        boolean enabled;
        long capacity = 100;
        double refillRate = 10.0;
        String keySource = "COMBINED";
    }
}
