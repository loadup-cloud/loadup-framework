package io.github.loadup.gateway.filter.bucket4j;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.caffeine.CaffeineProxyManager;
import io.github.bucket4j.distributed.proxy.AsyncProxyManager;
import java.time.Duration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/** Supplies local Bucket4j storage unless the application provides a distributed manager. */
@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Bucket4jGatewayAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(AsyncProxyManager.class)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public AsyncProxyManager<String> gatewayRateLimitProxyManager() {
        Caffeine cache = Caffeine.newBuilder().maximumSize(10_000);
        return new CaffeineProxyManager(cache, Duration.ofDays(2)).asAsync();
    }

    @Bean
    @ConditionalOnMissingBean(Bucket4jRateLimitAdapter.class)
    public Bucket4jRateLimitAdapter bucket4jRateLimitAdapter(AsyncProxyManager<String> buckets) {
        return new Bucket4jRateLimitAdapter(buckets);
    }
}
