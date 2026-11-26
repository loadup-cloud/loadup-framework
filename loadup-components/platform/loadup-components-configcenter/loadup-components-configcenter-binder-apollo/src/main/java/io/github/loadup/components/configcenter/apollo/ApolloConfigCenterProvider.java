/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.configcenter.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import io.github.loadup.components.configcenter.ConfigCenterListeners;
import io.github.loadup.components.configcenter.ConfigCenterProvider;
import java.util.List;
import java.util.function.Consumer;

public class ApolloConfigCenterProvider implements ConfigCenterProvider, AutoCloseable {
    private final Config apolloConfig;
    private final ApolloConfigPublisher publisher;
    private final ConfigCenterListeners listeners = new ConfigCenterListeners();
    private final ConfigChangeListener sdkListener = event -> event.changedKeys()
            .forEach(key -> listeners.dispatch(key, event.getChange(key).getNewValue()));

    public ApolloConfigCenterProvider(ApolloConfigCenterProperties config) {
        publisher = ApolloConfigPublisher.create(config);
        System.setProperty("app.id", config.getAppId());
        if (config.getMeta() != null) {
            System.setProperty("apollo.meta", config.getMeta());
        }
        if (config.getEnv() != null) {
            System.setProperty("env", config.getEnv());
        }
        if (config.getCluster() != null) {
            System.setProperty("apollo.cluster", config.getCluster());
        }
        apolloConfig = ConfigService.getConfig(config.getNamespace());
        apolloConfig.addChangeListener(sdkListener);
    }

    @Override
    public String getConfig(String key) {
        return apolloConfig.getProperty(key, null);
    }

    @Override
    public boolean setConfig(String key, String value) {
        return publisher != null && publisher.publish(key, value);
    }

    @Override
    public boolean removeConfig(String key) {
        return publisher != null && publisher.remove(key);
    }

    @Override
    public List<String> listKeys(String prefix) {
        return apolloConfig.getPropertyNames().stream()
                .filter(key -> key.startsWith(prefix))
                .sorted()
                .toList();
    }

    @Override
    public Runnable subscribe(String key, Consumer<String> listener) {
        return listeners.subscribe(key, listener);
    }

    @Override
    public String getBinderType() {
        return "apollo";
    }

    @Override
    public void close() {
        apolloConfig.removeChangeListener(sdkListener);
    }
}
