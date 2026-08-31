package io.github.loadup.components.cache.autoconfig;

/*-
 * #%L
 * Loadup Cache Components API
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import io.github.loadup.components.cache.LoadupCacheProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;

/**
 * LoadUp cache facade auto-configuration.
 *
 * <p>Enables Spring Cache annotation-driven caching so that business code can start using
 * {@code @Cacheable} right after adding a binder dependency. A matching binder auto-configuration
 * (caffeine / redis / jetcache) supplies the actual {@link CacheManager}; without any binder Spring
 * Boot falls back to a no-op manager.
 */
@AutoConfiguration(before = CacheAutoConfiguration.class)
@ConditionalOnClass(CacheManager.class)
@EnableCaching
@EnableConfigurationProperties(LoadupCacheProperties.class)
public class LoadupCacheAutoConfiguration {}
