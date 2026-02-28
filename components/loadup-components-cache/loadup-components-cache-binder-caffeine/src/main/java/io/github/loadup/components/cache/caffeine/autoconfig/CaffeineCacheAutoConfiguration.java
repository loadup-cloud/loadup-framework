package io.github.loadup.components.cache.caffeine.autoconfig;

/*-
 * #%L
 * Loadup Cache Binder Caffeine
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

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.loadup.components.cache.binding.impl.DefaultCacheBinding;
import io.github.loadup.components.cache.caffeine.binder.CaffeineCacheBinder;
import io.github.loadup.components.cache.caffeine.cfg.CaffeineCacheBinderCfg;
import io.github.loadup.components.cache.cfg.CacheBindingCfg;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(Caffeine.class)
public class CaffeineCacheAutoConfiguration {

    /**
     * 当 classpath 中存在 Caffeine 时，注册其元数据
     */
    @Bean
    public BindingMetadata<?, ?, ?, ?> caffeineMetadata() {
        return new BindingMetadata<>(
                "caffeine",
                DefaultCacheBinding.class,
                CaffeineCacheBinder.class,
                CacheBindingCfg.class,
                CaffeineCacheBinderCfg.class,
                ctx -> new DefaultCacheBinding());
    }
}
