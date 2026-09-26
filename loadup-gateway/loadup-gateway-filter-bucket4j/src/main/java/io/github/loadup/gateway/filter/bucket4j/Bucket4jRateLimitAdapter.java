package io.github.loadup.gateway.filter.bucket4j;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.AsyncProxyManager;
import io.github.loadup.gateway.api.model.ManagedRoute;
import io.github.loadup.gateway.webmvc.managed.ManagedFilterAdapter;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

/** Adds a Bucket4j policy to a managed SCG MVC route. */
public final class Bucket4jRateLimitAdapter implements ManagedFilterAdapter {
    private final AsyncProxyManager<String> buckets;

    public Bucket4jRateLimitAdapter(AsyncProxyManager<String> buckets) {
        this.buckets = buckets;
    }

    @Override
    public String name() {
        return "RateLimit";
    }

    @Override
    public void configure(RouterFunctions.Builder builder, ManagedRoute route, ManagedRoute.Filter filter) {
        if (!Set.of("capacity", "periodSeconds", "key")
                .containsAll(filter.args().keySet())) {
            throw new IllegalArgumentException("RateLimit accepts capacity, periodSeconds and key only");
        }
        long capacity = Long.parseLong(filter.args().getOrDefault("capacity", "0"));
        long periodSeconds = Long.parseLong(filter.args().getOrDefault("periodSeconds", "0"));
        String key = filter.args().getOrDefault("key", "ip");
        if (capacity < 1
                || periodSeconds < 1
                || periodSeconds > 86_400
                || !Set.of("ip", "principal").contains(key)) {
            throw new IllegalArgumentException("Invalid RateLimit configuration for route: " + route.id());
        }
        Duration period = Duration.ofSeconds(periodSeconds);
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, period)
                        .build())
                .build();
        builder.filter((request, next) -> {
            String identity = "principal".equals(key)
                    ? request.servletRequest().getUserPrincipal() == null
                            ? null
                            : request.servletRequest().getUserPrincipal().getName()
                    : request.servletRequest().getRemoteAddr();
            if (identity == null || identity.isBlank()) {
                return ServerResponse.status(HttpStatus.FORBIDDEN).build();
            }
            String bucketKey = route.id() + ":" + identity;
            CompletableFuture<ConsumptionProbe> consumption = buckets.builder()
                    .build(bucketKey, () -> CompletableFuture.completedFuture(configuration))
                    .tryConsumeAndReturnRemaining(1);
            ConsumptionProbe probe = consumption.get(5, TimeUnit.SECONDS);
            if (!probe.isConsumed()) {
                return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .header("X-RateLimit-Remaining", "0")
                        .build();
            }
            return next.handle(request);
        });
    }
}
