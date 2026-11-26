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
package io.github.loadup.components.configcenter.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigQueryResult;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.configcenter.ConfigCenterListeners;
import io.github.loadup.components.configcenter.ConfigCenterProvider;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

@SuppressFBWarnings(
        value = "CT_CONSTRUCTOR_THROW",
        justification = "ConfigService creation and initial load must fail fast at startup")
public class NacosConfigCenterProvider implements ConfigCenterProvider, AutoCloseable {
    private static final String EMPTY_DOCUMENT_MD5 = DigestUtils.md5DigestAsHex(new byte[0]);
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosConfigCenterProvider.class);

    private final ConfigService configService;
    private final NacosConfigCenterProperties config;
    private final ConfigCenterListeners listeners = new ConfigCenterListeners();
    private final Listener sdkListener = new Listener() {
        @Override
        public Executor getExecutor() {
            return Runnable::run;
        }

        @Override
        public void receiveConfigInfo(String content) {
            refreshAndDispatch(content);
        }
    };

    private volatile Map<String, String> snapshot = Map.of();

    public NacosConfigCenterProvider(NacosConfigCenterProperties config) {
        this.config = config;
        Properties props = new Properties();
        props.setProperty("serverAddr", config.getServerAddr());
        if (StringUtils.hasText(config.getNamespace())) {
            props.setProperty("namespace", config.getNamespace());
        }
        if (config.getUsername() != null) {
            props.setProperty("username", config.getUsername());
        }
        if (config.getPassword() != null) {
            props.setProperty("password", config.getPassword());
        }
        try {
            this.configService = NacosFactory.createConfigService(props);
        } catch (NacosException e) {
            throw new CommonException(CommonResultCodeEnum.SYS_ERROR, e);
        }

        try {
            initialize();
        } catch (NacosException | RuntimeException e) {
            try {
                close();
            } catch (NacosException | RuntimeException closeFailure) {
                e.addSuppressed(closeFailure);
            }
            throw new IllegalStateException("Could not initialize the Nacos configuration subscription", e);
        }
    }

    private synchronized void initialize() throws NacosException {
        String content = configService.getConfigAndSignListener(
                config.getDataId(), config.getGroup(), config.getTimeout(), sdkListener);
        snapshot = Map.copyOf(NacosConfigContent.parse(content, config.getFileExtension()));
    }

    @Override
    public String getConfig(String key) {
        return snapshot.get(key);
    }

    @Override
    public boolean setConfig(String key, String value) {
        Objects.requireNonNull(value, "Configuration value is required");
        return update(key, value);
    }

    @Override
    public boolean removeConfig(String key) {
        return update(key, null);
    }

    private boolean update(String key, String value) {
        try {
            ConfigQueryResult result =
                    configService.getConfigWithResult(config.getDataId(), config.getGroup(), config.getTimeout());
            Map<String, String> updated = NacosConfigContent.parse(result.getContent(), config.getFileExtension());
            if (value == null && updated.remove(key) == null) {
                return true;
            }
            if (value != null) {
                updated.put(key, value);
            }

            String content = NacosConfigContent.render(updated, config.getFileExtension());
            if (content.isBlank()) {
                content = "# Empty LoadUp configuration\n";
            }
            String type = "yml".equalsIgnoreCase(config.getFileExtension()) ? "yaml" : config.getFileExtension();
            // The server inserts on CAS when the document is absent. A concurrent nonempty
            // creation conflicts with the empty-document digest instead of losing its contents.
            String expectedMd5 = result.getContent() == null ? EMPTY_DOCUMENT_MD5 : result.getMd5();
            return StringUtils.hasText(expectedMd5)
                    && configService.publishConfigCas(
                            config.getDataId(), config.getGroup(), content, expectedMd5, type);
        } catch (NacosException | RuntimeException e) {
            LOGGER.warn("Nacos configuration update failed ({})", e.getClass().getSimpleName());
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
    public Runnable subscribe(String key, Consumer<String> listener) {
        return listeners.subscribe(key, listener);
    }

    @Override
    public String getBinderType() {
        return "nacos";
    }

    private void refreshAndDispatch(String content) {
        Map<String, String> updated;
        try {
            updated = Map.copyOf(NacosConfigContent.parse(content, config.getFileExtension()));
        } catch (RuntimeException e) {
            LOGGER.warn(
                    "Could not refresh the Nacos configuration snapshot ({})",
                    e.getClass().getSimpleName());
            return;
        }
        Map<String, String> previous;
        synchronized (this) {
            previous = snapshot;
            snapshot = updated;
        }
        updated.forEach((key, value) -> {
            if (!Objects.equals(value, previous.get(key))) {
                listeners.dispatch(key, value);
            }
        });
        for (String key : previous.keySet()) {
            if (!updated.containsKey(key)) {
                listeners.dispatch(key, null);
            }
        }
    }

    @Override
    public void close() throws NacosException {
        try {
            configService.removeListener(config.getDataId(), config.getGroup(), sdkListener);
        } finally {
            configService.shutDown();
        }
    }
}
