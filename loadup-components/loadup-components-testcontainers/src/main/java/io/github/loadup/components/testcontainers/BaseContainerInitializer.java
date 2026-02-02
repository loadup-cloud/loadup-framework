package io.github.loadup.components.testcontainers;

/*-
 * #%L
 * Loadup Components TestContainers
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

import io.github.loadup.components.testcontainers.config.TestContainersProperties;
import io.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

@Slf4j
public abstract class BaseContainerInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    ConfigurableEnvironment env = applicationContext.getEnvironment();

    // 1. 统一绑定配置对象
    TestContainersProperties properties =
        Binder.get(env)
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

  protected abstract void startAndApplyProperties(
      ContainerConfig config, ConfigurableEnvironment env);

  /** 辅助工具：应用属性到 Spring 环境 */
  protected void applyProperties(ConfigurableEnvironment env, Map<String, String> properties) {
    TestPropertyValues.of(properties).applyTo(env);
  }
}
