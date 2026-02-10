package io.github.loadup.components.gotone.core.manager;

/*-
 * #%L
 * loadup-components-gotone-core
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.components.gotone.api.NotificationChannelProvider;
import io.github.loadup.components.gotone.enums.NotificationChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * 通知渠道管理器
 *
 * <p>管理所有渠道提供商，支持按渠道类型和提供商名称查找
 */
@Slf4j
public class NotificationChannelManager {

    // channel -> (providerName -> provider)
    private final Map<NotificationChannel, Map<String, NotificationChannelProvider>> providerMap =
            new ConcurrentHashMap<>();

    public NotificationChannelManager(List<NotificationChannelProvider> providers) {
        if (providers != null) {
            for (NotificationChannelProvider provider : providers) {
                NotificationChannel channel = provider.getChannel();
                String providerName = provider.getProviderName();

                providerMap
                        .computeIfAbsent(channel, k -> new ConcurrentHashMap<>())
                        .put(providerName, provider);

                log.info(">>> [GOTONE] Registered provider: {} for channel: {}", providerName, channel);
            }
        }
        log.info(">>> [GOTONE] NotificationChannelManager initialized with {} channels", providerMap.size());
    }

    /**
     * 获取指定渠道和提供商的Provider
     */
    public NotificationChannelProvider getProvider(NotificationChannel channel, String providerName) {
        Map<String, NotificationChannelProvider> providers = providerMap.get(channel);
        if (providers == null || providers.isEmpty()) {
            log.error(">>> [GOTONE] No provider found for channel: {}", channel);
            return null;
        }

        NotificationChannelProvider provider = providers.get(providerName);
        if (provider == null) {
            log.warn(
                    ">>> [GOTONE] Provider {} not found for channel {}, available: {}",
                    providerName,
                    channel,
                    providers.keySet());
            // 返回第一个可用的provider作为降级
            provider = providers.values().stream().findFirst().orElse(null);
        }

        return provider != null && provider.isAvailable() ? provider : null;
    }

    /**
     * 获取指定渠道的默认Provider（第一个注册的）
     */
    public NotificationChannelProvider getProvider(NotificationChannel channel) {
        Map<String, NotificationChannelProvider> providers = providerMap.get(channel);
        if (providers == null || providers.isEmpty()) {
            log.error(">>> [GOTONE] No provider found for channel: {}", channel);
            return null;
        }

        return providers.values().stream()
                .filter(NotificationChannelProvider::isAvailable)
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查渠道是否支持
     */
    public boolean isChannelSupported(NotificationChannel channel) {
        Map<String, NotificationChannelProvider> providers = providerMap.get(channel);
        return providers != null && providers.values().stream().anyMatch(NotificationChannelProvider::isAvailable);
    }
}
