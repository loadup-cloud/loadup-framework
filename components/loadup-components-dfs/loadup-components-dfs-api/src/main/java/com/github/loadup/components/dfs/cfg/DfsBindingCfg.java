package com.github.loadup.components.dfs.cfg;

/*-
 * #%L
 * loadup-components-dfs-api
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

import com.github.loadup.commons.enums.BinderEnum;
import com.github.loadup.framework.api.cfg.BaseBindingCfg;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * LoadUp Cache Configuration Properties. Unified configuration for all dfs implementations with
 * per-dfs-name binder selection.
 *
 * <p>Configuration examples:
 *
 * <pre>
 * # Global default binder
 * loadup.dfs.binder=redis
 *
 * # Per-dfs-name binder selection
 * loadup.dfs.binders.userCache=redis
 * loadup.dfs.binders.productCache=caffeine
 *
 * # Per-dfs-name common configurations
 * loadup.dfs.dfs-configs.userCache.maximum-size=10000
 * loadup.dfs.dfs-configs.userCache.expire-after-write=30m
 * loadup.dfs.dfs-configs.productCache.maximum-size=5000
 * loadup.dfs.dfs-configs.productCache.expire-after-write=1h
 *
 * # Binder-specific configurations
 * # Redis binder configuration
 * loadup.dfs.binder.redis.host=localhost
 * loadup.dfs.binder.redis.port=6379
 * loadup.dfs.binder.redis.password=secret
 *
 * # Caffeine binder configuration
 * loadup.dfs.binder.caffeine.spec=maximumSize=1000,expireAfterWrite=300s
 * </pre>
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "loadup.dfs")
public class DfsBindingCfg extends BaseBindingCfg {
  /** 默认使用的 Binder 类型 */
  private BinderEnum.DfsBinder binder;

  /** 启用的 Binder 列表 (可选) */
  private List<BinderEnum.DfsBinder> enabledBinders = new ArrayList<>();

  /** 业务标识到 Binder 类型的映射 (可选) */
  private Map<String, BinderEnum.DfsBinder> mappings = new HashMap<>();

  private Map<String, Map<String, String>> configs = new HashMap<>();
}
