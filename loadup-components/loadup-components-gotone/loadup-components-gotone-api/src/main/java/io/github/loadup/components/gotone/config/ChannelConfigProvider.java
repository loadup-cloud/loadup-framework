package io.github.loadup.components.gotone.config;

/*-
 * #%L
 * Loadup Gotone API
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import java.util.List;
import java.util.Map;

/**
 * Optional storage SPI that resolves the enabled channel configuration for a service code.
 *
 * <p>The engine treats this provider as optional: when absent, requests may still be sent directly
 * to channels listed in {@code NotificationRequest.channels()}.
 */
public interface ChannelConfigProvider {

    /**
     * Returns the enabled channel configurations for the given service code.
     *
     * @param serviceCode the service code
     * @return the enabled channel configurations, never {@code null}
     */
    List<ChannelConfig> findEnabledByServiceCode(String serviceCode);

    /**
     * Channel configuration resolved for one service code.
     *
     * @param channel the channel type, e.g. EMAIL or SMS
     * @param provider the preferred provider name
     * @param fallbackProviders the ordered fallback provider names
     * @param templateContent the raw template content, or {@code null} when not templated
     * @param channelConfig the channel-specific configuration map (subject, sign name, webhook URL)
     */
    record ChannelConfig(
            String channel,
            String provider,
            List<String> fallbackProviders,
            String templateContent,
            Map<String, Object> channelConfig) {

        public ChannelConfig {
            fallbackProviders = fallbackProviders == null ? List.of() : List.copyOf(fallbackProviders);
            channelConfig = channelConfig == null ? Map.of() : Map.copyOf(channelConfig);
        }
    }
}
