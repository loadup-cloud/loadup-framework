package io.github.loadup.components.cache.autoconfig;

/*-
 * #%L
 * Loadup Cache Components API
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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
