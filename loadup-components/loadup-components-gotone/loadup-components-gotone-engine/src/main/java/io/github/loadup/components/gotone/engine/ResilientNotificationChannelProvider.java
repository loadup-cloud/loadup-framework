package io.github.loadup.components.gotone.engine;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import io.github.loadup.components.resilience4j.ResilienceRegistries;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

/**
 * Wraps a {@link NotificationChannelProvider} with a per-provider circuit breaker and retry.
 *
 * <p>The circuit breaker wraps the whole retry loop, so retrying an unhealthy provider is bounded
 * and an exhausted retry trips the breaker exactly once; the existing fallback chain then moves to
 * the next provider.
 */
public final class ResilientNotificationChannelProvider implements NotificationChannelProvider {

    private final NotificationChannelProvider delegate;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    private ResilientNotificationChannelProvider(
            NotificationChannelProvider delegate, CircuitBreaker circuitBreaker, Retry retry) {
        this.delegate = delegate;
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
    }

    /**
     * Wraps the given provider with registries-backed circuit breaker and retry instances named
     * {@code gotone-<channelType>-<providerName>}.
     *
     * @param delegate the raw provider
     * @param registries the resilience4j registries
     * @return the wrapped provider
     */
    public static ResilientNotificationChannelProvider wrap(
            NotificationChannelProvider delegate, ResilienceRegistries registries) {
        String instanceName = instanceName(delegate);
        CircuitBreaker circuitBreaker = registries.circuitBreakerRegistry().circuitBreaker(instanceName);
        Retry retry = registries.retryRegistry().retry(instanceName);
        return new ResilientNotificationChannelProvider(delegate, circuitBreaker, retry);
    }

    /**
     * Returns the resilience instance name for a provider.
     *
     * @param provider the provider
     * @return the instance name
     */
    public static String instanceName(NotificationChannelProvider provider) {
        return "gotone-" + provider.getChannelType() + "-" + provider.getProviderName();
    }

    @Override
    public String getChannelType() {
        return delegate.getChannelType();
    }

    @Override
    public String getProviderName() {
        return delegate.getProviderName();
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        return circuitBreaker.executeSupplier(() -> retry.executeSupplier(() -> delegate.send(request)));
    }

    @Override
    public boolean isAvailable() {
        return delegate.isAvailable() && circuitBreaker.getState() != CircuitBreaker.State.OPEN;
    }
}
