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

import io.github.loadup.components.cache.CacheProperties;
import io.github.loadup.components.cache.CacheProvider;
import io.github.loadup.components.cache.CacheTemplate;
import io.github.loadup.components.cache.DefaultCacheTemplate;
import io.github.loadup.components.cache.serializer.CacheSerializer;
import io.github.loadup.components.cache.serializer.JsonCacheSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnSingleCandidate(CacheProvider.class)
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CacheSerializer.class)
    public CacheSerializer cacheSerializer() {
        return new JsonCacheSerializer();
    }

    @Bean
    @ConditionalOnMissingBean(CacheTemplate.class)
    public CacheTemplate cacheTemplate(CacheProvider provider, CacheSerializer serializer, CacheProperties props) {
        return new DefaultCacheTemplate(provider, serializer, props.getKeyPrefix());
    }
}
