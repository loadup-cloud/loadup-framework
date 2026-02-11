package io.github.loadup.components.scheduler.autoconfig;

/*-
 * #%L
 * Loadup Cache Starter
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

import io.github.loadup.components.scheduler.initializer.SchedulerJobInitializer;
import io.github.loadup.components.scheduler.manager.SchedulerBindingManager;
import io.github.loadup.components.scheduler.properties.SchedulerGroupProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(SchedulerGroupProperties.class)
public class SchedulerBindingAutoConfiguration {
    /**
     * 将 SchedulerBindingManager 定义为一个单例 Bean 依赖的 props 和 context 会由 Spring 自动注入到方法参数中
     */
    @Bean
    @ConditionalOnMissingBean
    public SchedulerBindingManager bindingManager(SchedulerGroupProperties props, ApplicationContext context) {
        return new SchedulerBindingManager(props, context);
    }
    /** 核心：注册初始化器，它会自动处理注解任务 */
    @Bean
    @ConditionalOnMissingBean
    public SchedulerJobInitializer schedulerJobInitializer(
            ApplicationContext context, SchedulerBindingManager manager) {
        return new SchedulerJobInitializer(context, manager);
    }
}
