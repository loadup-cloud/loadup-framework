package com.github.loadup.components.extension.config;

/*-
 * #%L
 * loadup-components-extension
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.github.loadup.components.extension.exector.ExtensionExecutor;
import com.github.loadup.components.extension.register.ExtensionRegistry;

/** Extension 框架的 Spring Boot 自动装配类 */
@AutoConfiguration
@ConditionalOnClass({ExtensionExecutor.class, ExtensionRegistry.class}) // 条件：只有当核心类存在时，此配置才生效
public class ExtensionAutoConfiguration {

  /**
   * 自动配置 ExtensionRegistry Bean @ConditionalOnMissingBean: 允许用户覆盖默认的 Bean。如果用户自己定义了一个
   * ExtensionRegistry，则此配置不生效。
   */
  @Bean
  @ConditionalOnMissingBean
  public ExtensionRegistry extensionRegistry() {
    return new ExtensionRegistry();
  }

  /**
   * 自动配置 ExtensionExecutor Bean
   *
   * @param extensionRegistry Spring会自动从上下文中注入上面创建的 extensionRegistry
   *     Bean @ConditionalOnMissingBean: 同样允许用户自定义覆盖。
   */
  @Bean
  @ConditionalOnMissingBean
  public ExtensionExecutor extensionExecutor(ExtensionRegistry extensionRegistry) {
    return new ExtensionExecutor(extensionRegistry);
  }
}
