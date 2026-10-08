package io.github.loadup.components.gotone.config;

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
