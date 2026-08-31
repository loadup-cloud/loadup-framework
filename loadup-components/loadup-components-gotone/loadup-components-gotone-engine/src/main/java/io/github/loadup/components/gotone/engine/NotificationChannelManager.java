package io.github.loadup.components.gotone.engine;

/*-
 * #%L
 * Loadup Gotone Engine
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationChannelManager {
    private static final Logger log = LoggerFactory.getLogger(NotificationChannelManager.class);

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

    public SendResult sendWithFallback(
            String channelType, String primaryProvider, List<String> fallbackChain, ChannelSendRequest request) {
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
