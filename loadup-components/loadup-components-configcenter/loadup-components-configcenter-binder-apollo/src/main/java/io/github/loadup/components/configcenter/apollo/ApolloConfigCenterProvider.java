package io.github.loadup.components.configcenter.apollo;

/*-
 * #%L
 * LoadUp ConfigCenter Binder Apollo
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
