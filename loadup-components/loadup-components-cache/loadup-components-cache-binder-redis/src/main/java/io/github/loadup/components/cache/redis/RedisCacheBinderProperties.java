package io.github.loadup.components.cache.redis;

/*-
 * #%L
 * Loadup Cache Binder Redis
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

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Redis binder settings ({@code loadup.cache.binder.redis.*}). */
@ConfigurationProperties(prefix = "loadup.cache.binder.redis")
public class RedisCacheBinderProperties {

    /** Key prefix applied to every cache name (Redis keys look like {@code <prefix><name>::<key>}). */
    private String keyPrefix = "loadup:cache:";

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
}
