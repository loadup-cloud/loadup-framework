package io.github.loadup.components.cache.autoconfig;

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

import io.github.loadup.components.cache.binder.CacheTicker;
import io.github.loadup.components.cache.constants.CacheConstants;
import io.github.loadup.components.cache.manager.CacheBindingManager;
import io.github.loadup.components.cache.properties.CacheGroupProperties;
import io.github.loadup.components.cache.serializer.CacheSerializer;
import io.github.loadup.components.cache.serializer.JsonCacheSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(CacheGroupProperties.class)
public class CacheBindingAutoConfiguration {
    /**
     * 将 CacheBindingManager 定义为一个单例 Bean 依赖的 props 和 context 会由 Spring 自动注入到方法参数中
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheBindingManager cacheBindingManager(CacheGroupProperties props, ApplicationContext context) {
        return new CacheBindingManager(props, context);
    }

    /**
     * 注册默认的时间源 使用 @ConditionalOnMissingBean 允许用户提供自己的实现来覆盖它
     */
    @Bean(name = CacheConstants.DEFAULT_TICKER)
    @ConditionalOnMissingBean(name = CacheConstants.DEFAULT_TICKER)
    public CacheTicker defaultCacheTicker() {
        return CacheTicker.SYSTEM;
    }

    /**
     * 注册默认的 JSON 序列化器 使用 @ConditionalOnMissingBean 允许用户提供自己的实现来覆盖它
     */
    @Bean(name = CacheConstants.SERIALIZER_JSON)
    @ConditionalOnMissingBean(CacheSerializer.class)
    public CacheSerializer defaultJsonCacheSerializer() {
        return new JsonCacheSerializer();
    }


}
