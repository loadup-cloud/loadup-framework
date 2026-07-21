package io.github.loadup.components.gotone.config;

import java.util.List;
import java.util.Map;

public interface ChannelConfigProvider {
    List<ChannelConfig> findEnabledByServiceCode(String serviceCode);

    record ChannelConfig(
            String channel,
            String provider,
            List<String> fallbackProviders,
            String templateContent,
            Map<String, Object> channelConfig,
            Map<String, Object> retryConfig) {}
}
