package io.github.loadup.components.testcontainers;

/*-
 * #%L
 * Loadup Components TestContainers
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

import io.github.loadup.components.testcontainers.config.TestContainersProperties;
import io.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public abstract class BaseContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger log = LoggerFactory.getLogger(BaseContainerInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();

        // 1. 统一绑定配置对象
        TestContainersProperties properties = Binder.get(env)
                .bind("loadup.testcontainers", TestContainersProperties.class)
                .orElseGet(TestContainersProperties::new);

        // 2. 获取具体子类的容器配置
        ContainerConfig config = getContainerConfig(properties);

        // 3. 检查开关
        if (properties.isEnabled() && config.isEnabled()) {
            log.info("🚀 Initializing TestContainer for: {}", getContainerName());

            // 启动并注入属性
            startAndApplyProperties(config, env);
        } else {
            log.info("⏭️ TestContainer for {} is disabled.", getContainerName());
        }
    }

    protected abstract String getContainerName();

    protected abstract ContainerConfig getContainerConfig(TestContainersProperties properties);

    protected abstract void startAndApplyProperties(ContainerConfig config, ConfigurableEnvironment env);

    /**
     * 辅助工具：应用属性到 Spring 环境
     */
    protected void applyProperties(ConfigurableEnvironment env, Map<String, String> properties) {
        TestPropertyValues.of(properties).applyTo(env);
    }
}
