/*-
 * #%L
 * Loadup Gotone API
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
