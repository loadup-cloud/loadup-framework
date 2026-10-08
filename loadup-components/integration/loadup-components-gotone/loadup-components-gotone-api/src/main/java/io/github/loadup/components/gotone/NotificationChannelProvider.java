package io.github.loadup.components.gotone;

import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;

/**
 * SPI implemented by every channel binder (email, sms, push, webhook, ...).
 *
 * <p>Providers are registered as Spring beans and collected by the engine. A provider reports its
 * channel type (EMAIL, SMS, PUSH, WEBHOOK) and a provider name, which are used by the engine's
 * fallback chain and by resilience wrappers.
 */
public interface NotificationChannelProvider {

    /**
     * Returns the channel type this provider serves, e.g. {@code EMAIL} or {@code SMS}.
     *
     * @return the channel type
     */
    String getChannelType();

    /**
     * Returns the provider name, unique within a channel type, e.g. {@code smtp} or {@code aliyun}.
     *
     * @return the provider name
     */
    String getProviderName();

    /**
     * Sends one notification through this provider.
     *
     * @param request the channel-level send request
     * @return the per-receiver result
     */
    ChannelSendResponse send(ChannelSendRequest request);

    /**
     * Returns whether this provider is currently usable (e.g. configured, or its circuit breaker
     * is not open).
     *
     * @return {@code true} when the provider can be used
     */
    boolean isAvailable();
}
