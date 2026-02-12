package io.github.loadup.components.cache.cfg;

/*-
 * #%L
 * Loadup Cache Components API
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

import io.github.loadup.components.cache.constants.CacheConstants;
import io.github.loadup.framework.api.cfg.BaseBinderCfg;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lise
 * @version BaseCacheBindingCfg.java, v 0.1 2026年01月13日 18:02 lise
 */
@Getter
@Setter
public abstract class CacheBinderCfg extends BaseBinderCfg {
    // 指定序列化器的 Bean 名称，例如 "defaultJsonCacheSerializer" 或 "kryoSerializer"
    private String serializerBeanName = CacheConstants.SERIALIZER_JSON;
    // 默认使用系统时间源
    private String tickerBeanName = CacheConstants.DEFAULT_TICKER;

}
