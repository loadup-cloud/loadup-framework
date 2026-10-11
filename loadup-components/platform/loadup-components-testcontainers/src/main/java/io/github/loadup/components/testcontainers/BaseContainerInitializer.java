/*
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
package io.github.loadup.components.testcontainers;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.testcontainers.config.TestContainersProperties;
import io.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import java.util.Map;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public abstract class BaseContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();

        TestContainersProperties properties = Binder.get(env)
                .bind("loadup.testcontainers", TestContainersProperties.class)
                .orElseGet(TestContainersProperties::new);

        ContainerConfig config = getContainerConfig(properties);

        if (properties.isEnabled() && config.isEnabled()) {
            LogUtil.info(BaseContainerInitializer.class, "🚀 Initializing TestContainer for: {}", getContainerName());

            startAndApplyProperties(config, env);
        } else {
            LogUtil.info(BaseContainerInitializer.class, "⏭️ TestContainer for {} is disabled.", getContainerName());
        }
    }

    protected abstract String getContainerName();

    protected abstract ContainerConfig getContainerConfig(TestContainersProperties properties);

    protected abstract void startAndApplyProperties(ContainerConfig config, ConfigurableEnvironment env);

    protected void applyProperties(ConfigurableEnvironment env, Map<String, String> properties) {
        TestPropertyValues.of(properties).applyTo(env);
    }
}
