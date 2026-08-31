package io.github.loadup.components.configcenter.nacos;

/*-
 * #%L
 * LoadUp ConfigCenter Binder Nacos
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

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import io.github.loadup.components.configcenter.ConfigCenterProvider;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class NacosConfigCenterProvider implements ConfigCenterProvider {
    private final ConfigService configService;
    private final String dataId = "loadup-config";
    private final String group = "DEFAULT_GROUP";
    private final ConcurrentHashMap<String, List<Consumer<String>>> listeners = new ConcurrentHashMap<>();

    public NacosConfigCenterProvider(NacosConfigCenterConfig config) {
        Properties props = new Properties();
        props.setProperty("serverAddr", config.getServerAddr());
        props.setProperty("namespace", config.getNamespace());
        if (config.getUsername() != null) props.setProperty("username", config.getUsername());
        if (config.getPassword() != null) props.setProperty("password", config.getPassword());
        try {
            this.configService = NacosFactory.createConfigService(props);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Nacos ConfigService", e);
        }
    }

    @Override
    public String getConfig(String key) {
        try {
            return configService.getConfig(dataId, group, 3000);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean setConfig(String key, String value) {
        try {
            return configService.publishConfig(dataId, group, value);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean removeConfig(String key) {
        try {
            return configService.removeConfig(dataId, group);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> listKeys(String prefix) {
        return Collections.emptyList();
    }

    @Override
    public void addListener(String key, Consumer<String> listener) {
        String content = getConfig(key);
        listeners
                .computeIfAbsent(key, k -> Collections.synchronizedList(new java.util.ArrayList<>()))
                .add(listener);
        try {
            configService.addListener(dataId, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return Runnable::run;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    listeners.get(key).forEach(l -> l.accept(configInfo));
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to add Nacos listener", e);
        }
    }

    @Override
    public void removeListener(String key) {
        listeners.remove(key);
    }

    @Override
    public String getBinderType() {
        return "nacos";
    }
}
