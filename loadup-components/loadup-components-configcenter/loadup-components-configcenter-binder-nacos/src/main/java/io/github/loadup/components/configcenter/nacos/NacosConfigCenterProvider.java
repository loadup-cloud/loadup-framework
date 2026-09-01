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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.loadup.components.configcenter.ConfigCenterProvider;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Nacos-backed {@link ConfigCenterProvider}.
 *
 * <p>Nacos stores one file per {@code dataId}; this provider loads the whole file into a flat
 * key-value snapshot (see {@link NacosConfigContent}) and serves individual keys from it. Writes
 * are read-modify-write of the whole file (last-writer-wins per file, not per key). A single
 * Nacos listener refreshes the snapshot and dispatches only the keys whose values actually
 * changed.
 */
@SuppressFBWarnings(
        value = "CT_CONSTRUCTOR_THROW",
        justification = "ConfigService creation and initial load must fail fast at startup")
public class NacosConfigCenterProvider implements ConfigCenterProvider {
    private final ConfigService configService;
    private final NacosConfigCenterConfig config;
    private final ConcurrentHashMap<String, List<Consumer<String>>> listeners = new ConcurrentHashMap<>();
    private final AtomicBoolean nacosListenerRegistered = new AtomicBoolean();
    private volatile Map<String, String> snapshot = Collections.emptyMap();

    public NacosConfigCenterProvider(NacosConfigCenterConfig config) {
        this.config = config;
        Properties props = new Properties();
        props.setProperty("serverAddr", config.getServerAddr());
        if (config.getNamespace() != null && !config.getNamespace().isBlank()) {
            props.setProperty("namespace", config.getNamespace());
        }
        if (config.getUsername() != null) props.setProperty("username", config.getUsername());
        if (config.getPassword() != null) props.setProperty("password", config.getPassword());
        try {
            this.configService = NacosFactory.createConfigService(props);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Nacos ConfigService", e);
        }
        this.snapshot = loadSnapshot();
    }

    @Override
    public String getConfig(String key) {
        return snapshot.get(key);
    }

    @Override
    public boolean setConfig(String key, String value) {
        try {
            Map<String, String> updated = new LinkedHashMap<>(loadSnapshot());
            updated.put(key, value);
            boolean published = configService.publishConfig(
                    config.getDataId(),
                    config.getGroup(),
                    NacosConfigContent.render(updated, config.getFileExtension()));
            if (published) {
                snapshot = Collections.unmodifiableMap(updated);
            }
            return published;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean removeConfig(String key) {
        try {
            Map<String, String> updated = new LinkedHashMap<>(loadSnapshot());
            String removed = updated.remove(key);
            if (removed == null) {
                return true;
            }
            boolean published = configService.publishConfig(
                    config.getDataId(),
                    config.getGroup(),
                    NacosConfigContent.render(updated, config.getFileExtension()));
            if (published) {
                snapshot = Collections.unmodifiableMap(updated);
            }
            return published;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> listKeys(String prefix) {
        return snapshot.keySet().stream()
                .filter(key -> key.startsWith(prefix))
                .sorted()
                .toList();
    }

    @Override
    public void addListener(String key, Consumer<String> listener) {
        listeners
                .computeIfAbsent(key, k -> Collections.synchronizedList(new java.util.ArrayList<>()))
                .add(listener);
        registerNacosListener();
    }

    @Override
    public void removeListener(String key) {
        listeners.remove(key);
    }

    @Override
    public String getBinderType() {
        return "nacos";
    }

    private void registerNacosListener() {
        if (!nacosListenerRegistered.compareAndSet(false, true)) {
            return;
        }
        try {
            configService.addListener(config.getDataId(), config.getGroup(), new Listener() {
                @Override
                public Executor getExecutor() {
                    return Runnable::run;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    refreshAndDispatch(configInfo);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to add Nacos listener", e);
        }
    }

    private Map<String, String> loadSnapshot() {
        try {
            String content = configService.getConfig(config.getDataId(), config.getGroup(), config.getTimeout());
            return Collections.unmodifiableMap(NacosConfigContent.parse(content, config.getFileExtension()));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load Nacos config dataId=" + config.getDataId(), e);
        }
    }

    private void refreshAndDispatch(String content) {
        Map<String, String> updated = NacosConfigContent.parse(content, config.getFileExtension());
        Map<String, String> previous = snapshot;
        if (updated.equals(previous)) {
            return;
        }
        snapshot = Collections.unmodifiableMap(updated);
        for (Map.Entry<String, String> entry : updated.entrySet()) {
            String key = entry.getKey();
            String oldValue = previous.get(key);
            String newValue = entry.getValue();
            if (!java.util.Objects.equals(oldValue, newValue)) {
                dispatch(key, newValue);
            }
        }
        for (String key : previous.keySet()) {
            if (!updated.containsKey(key)) {
                dispatch(key, null);
            }
        }
    }

    private void dispatch(String key, String value) {
        List<Consumer<String>> keyListeners = listeners.get(key);
        if (keyListeners != null) {
            keyListeners.forEach(listener -> listener.accept(value));
        }
    }
}
