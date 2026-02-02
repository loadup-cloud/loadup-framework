package io.github.loadup.testify.infra.container.manager;

/*-
 * #%L
 * Testify Infra Common
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

import io.github.loadup.testify.infra.container.InfraProvider;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.SpringFactoriesLoader;

@Slf4j
public class ContainerManager {

    private final Map<String, InfraProvider> availableProviders = new HashMap<>();
    private final List<InfraProvider> activeProviders = Collections.synchronizedList(new ArrayList<>());
    private final AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);

    public ContainerManager() {
        loadProviders();
    }

    /** 使用 SpringFactoriesLoader 加载类路径下所有的 InfraProvider 实现 */
    private void loadProviders() {
        List<InfraProvider> providers = SpringFactoriesLoader.loadFactories(
                InfraProvider.class, Thread.currentThread().getContextClassLoader());

        for (InfraProvider provider : providers) {
            availableProviders.put(provider.getType().toLowerCase(), provider);
            log.info(
                    ">>> [TESTIFY-INFRA] Registered provider: [{}] from class [{}]",
                    provider.getType(),
                    provider.getClass().getName());
        }
    }

    /** 根据配置初始化并启动容器 */
    public void init(List<Map<String, Object>> serviceConfigs, boolean globalReuse) {
        if (serviceConfigs == null || serviceConfigs.isEmpty()) {
            return;
        }

        for (Map<String, Object> config : serviceConfigs) {
            String type = Optional.ofNullable(config.get("type"))
                    .map(Object::toString)
                    .map(String::toLowerCase)
                    .orElse(null);

            if (type == null) continue;

            InfraProvider provider = availableProviders.get(type);
            if (provider != null) {
                log.info(">>> [TESTIFY-INFRA] Starting infrastructure: [{}]", type);
                try {
                    provider.start(config, globalReuse);
                    activeProviders.add(provider);
                } catch (Exception e) {
                    log.error(">>> [TESTIFY-INFRA] Failed to start container for [{}]: {}", type, e.getMessage(), e);
                    throw new RuntimeException("Infrastructure startup failed: " + type, e);
                }
            } else {
                log.warn(">>> [TESTIFY-INFRA] No provider implementation found for type: [{}]", type);
            }
        }

        // 如果开启了全局复用，不注册 Shutdown Hook，或者在 Hook 中判断
        if (!globalReuse) {
            ensureShutdownHook();
        } else {
            log.info(">>> [TESTIFY-INFRA] Global Reuse is ENABLED. Containers will persist after test.");
        }
    }

    /** 获取所有已启动容器暴露的属性 */
    public Map<String, String> getAllProperties() {
        Map<String, String> allProps = new HashMap<>();
        for (InfraProvider provider : activeProviders) {
            allProps.putAll(provider.getExposedProperties());
        }
        return allProps;
    }

    /** 注册 JVM 销毁钩子，确保进程退出时关闭容器 */
    private void ensureShutdownHook() {
        if (shutdownHookRegistered.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info(">>> [TESTIFY-INFRA] JVM Shutdown detected. Cleaning up containers...");
                activeProviders.forEach(provider -> {
                    try {
                        log.info(">>> [TESTIFY-INFRA] Stopping: [{}]", provider.getType());
                        provider.stop();
                    } catch (Exception e) {
                        log.error(
                                ">>> [TESTIFY-INFRA] Error stopping provider [{}]: {}",
                                provider.getType(),
                                e.getMessage());
                    }
                });
            }));
        }
    }
}
