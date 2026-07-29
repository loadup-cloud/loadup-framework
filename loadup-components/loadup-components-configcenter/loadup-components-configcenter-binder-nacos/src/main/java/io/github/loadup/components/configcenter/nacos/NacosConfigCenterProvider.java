package io.github.loadup.components.configcenter.nacos;

/*-
 * #%L
 * LoadUp ConfigCenter Binder Nacos
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
