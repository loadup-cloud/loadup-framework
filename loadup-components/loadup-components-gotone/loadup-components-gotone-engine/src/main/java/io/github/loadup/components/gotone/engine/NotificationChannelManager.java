package io.github.loadup.components.gotone.engine;

/*-
 * #%L
 * Loadup Gotone Engine
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import io.github.loadup.components.gotone.GotoneProvider;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotificationChannelManager {
    private final Map<String, Map<String, GotoneProvider>> providerMap = new ConcurrentHashMap<>();

    public NotificationChannelManager(List<GotoneProvider> providers) {
        if (providers != null) {
            for (GotoneProvider p : providers) {
                providerMap
                    .computeIfAbsent(p.getChannelType(), k -> new ConcurrentHashMap<>())
                    .put(p.getProviderName(), p);
                log.info("Registered provider: {} for channel: {}", p.getProviderName(), p.getChannelType());
            }
        }
    }

    public SendResult sendWithFallback(String channelType, String primaryProvider,
                                       List<String> fallbackChain, ChannelSendRequest request) {
        List<GotoneProvider> chain = buildProviderChain(channelType, primaryProvider, fallbackChain);
        List<Attempt> attempts = new ArrayList<>();

        for (GotoneProvider provider : chain) {
            try {
                ChannelSendResponse response = provider.send(request);
                boolean success = response.getSuccessCount() != null && response.getSuccessCount() > 0;
                attempts.add(new Attempt(provider.getProviderName(), success, null));
                if (success) {
                    log.info("Provider {} succeeded after {} attempt(s)", provider.getProviderName(), attempts.size());
                    return new SendResult(response, attempts, true);
                }
            } catch (Exception e) {
                attempts.add(new Attempt(provider.getProviderName(), false, e.getMessage()));
                log.warn("Provider {} failed ({}), trying next", provider.getProviderName(), e.getMessage());
            }
        }
        return new SendResult(null, attempts, false);
    }

    private List<GotoneProvider> buildProviderChain(String channelType, String primary, List<String> fallbackChain) {
        Map<String, GotoneProvider> providers = providerMap.get(channelType);
        if (providers == null || providers.isEmpty()) return List.of();

        Set<String> ordered = new LinkedHashSet<>();
        if (primary != null) ordered.add(primary);
        if (fallbackChain != null) {
            for (String name : fallbackChain) {
                if (!name.equals(primary)) ordered.add(name);
            }
        }
        providers.keySet().stream().filter(n -> !ordered.contains(n)).forEach(ordered::add);

        return ordered.stream().map(providers::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public record SendResult(ChannelSendResponse response, List<Attempt> attempts, boolean success) {
        public String getSuccessfulProvider() {
            if (!success || attempts.isEmpty()) return null;
            return attempts.get(attempts.size() - 1).providerName();
        }
    }
    public record Attempt(String providerName, boolean success, String error) {}
}
