package com.github.loadup.components.cache.config;

/*-
 * #%L
 * loadup-components-cache-api
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import com.github.loadup.components.cache.api.CacheBinder;
import com.github.loadup.components.cache.binding.DefaultCacheBinding;
import com.github.loadup.components.cache.cfg.CacheBindingCfg;
import com.github.loadup.framework.api.manager.BinderManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** LoadUp Cache Auto Configuration */
@Configuration
@EnableConfigurationProperties(CacheBindingCfg.class)
public class CacheAutoConfiguration {
  @Bean
  @ConditionalOnMissingBean
  public BinderManager<CacheBinder> cacheBinderManager(ObjectProvider<CacheBinder> provider) {
    return new BinderManager<>(provider, CacheBinder.class);
  }

  @Bean("cacheBinding")
  public DefaultCacheBinding cacheBinding(
      BinderManager<CacheBinder> binderManager, CacheBindingCfg cacheBindingCfg) {
    return new DefaultCacheBinding(binderManager, cacheBindingCfg);
  }
}
