package com.github.loadup.components.cache.cfg;

/*-
 * #%L
 * loadup-components-cache-api
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
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

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadUpCacheConfig {

  /** Maximum cache size */
  private long maximumSize;

  /** Base expiration time after write (e.g., "30m", "1h", "2d") */
  private String expireAfterWrite;

  /** Base expiration time after access (e.g., "30m", "1h", "2d") */
  private String expireAfterAccess;

  /** Enable random expiration offset to prevent cache avalanche Default: true */
  private boolean enableRandomExpiration = true;

  /**
   * Random expiration offset range in seconds The actual expiration time will be: baseExpiration +
   * random(0, randomOffsetSeconds) This helps prevent cache avalanche by distributing expiration
   * times Default: 60 seconds (1 minute)
   */
  private long randomOffsetSeconds = 60;

  /** Enable null value caching to prevent cache penetration Default: true */
  private boolean cacheNullValues = true;

  /** Expiration time for null values (shorter than normal values) Default: "5m" (5 minutes) */
  private String nullValueExpireAfterWrite = "5m";

  /** Cache warming strategy: preload data on startup Default: false */
  private boolean enableWarmup = false;

  /**
   * Cache priority level (1-10, higher is more important) Used for cache eviction priority Default:
   * 5
   */
  private int priority = 5;
}
