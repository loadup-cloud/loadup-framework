package io.github.loadup.components.configcenter.apollo;

/*-
 * #%L
 * LoadUp ConfigCenter Binder Apollo
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import io.github.loadup.components.configcenter.ConfigCenterProvider;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ApolloConfigCenterProvider implements ConfigCenterProvider {
    private final Config apolloConfig;
    private final ConcurrentHashMap<String, List<Consumer<String>>> listeners = new ConcurrentHashMap<>();

    public ApolloConfigCenterProvider(ApolloConfigCenterConfig config) {
        System.setProperty("app.id", config.getAppId());
        if (config.getMeta() != null) System.setProperty("apollo.meta", config.getMeta());
        if (config.getEnv() != null) System.setProperty("env", config.getEnv());
        if (config.getCluster() != null) System.setProperty("idc", config.getCluster());
        this.apolloConfig = ConfigService.getConfig(config.getNamespace());

        this.apolloConfig.addChangeListener(changeEvent -> {
            Set<String> keys = changeEvent.changedKeys();
            for (String key : keys) {
                List<Consumer<String>> ls = listeners.get(key);
                if (ls != null) {
                    String newValue = changeEvent.getChange(key).getNewValue();
                    ls.forEach(l -> l.accept(newValue));
                }
            }
        });
    }

    @Override
    public String getConfig(String key) {
        return apolloConfig.getProperty(key, null);
    }

    @Override
    public boolean setConfig(String key, String value) {
        return false;
    }

    @Override
    public boolean removeConfig(String key) {
        return false;
    }

    @Override
    public List<String> listKeys(String prefix) {
        return Collections.emptyList();
    }

    @Override
    public void addListener(String key, Consumer<String> listener) {
        listeners
                .computeIfAbsent(key, k -> Collections.synchronizedList(new java.util.ArrayList<>()))
                .add(listener);
    }

    @Override
    public void removeListener(String key) {
        listeners.remove(key);
    }

    @Override
    public String getBinderType() {
        return "apollo";
    }
}
