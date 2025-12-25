/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.cache.cfg;

/*-
 * #%L
 * loadup-components-cache-api
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadUpCacheConfig {

    /**
     * Maximum cache size
     */
    private long maximumSize;

    /**
     * Base expiration time after write (e.g., "30m", "1h", "2d")
     */
    private String expireAfterWrite;

    /**
     * Base expiration time after access (e.g., "30m", "1h", "2d")
     */
    private String expireAfterAccess;

    /**
     * Enable random expiration offset to prevent cache avalanche
     * Default: true
     */
    private boolean enableRandomExpiration = true;

    /**
     * Random expiration offset range in seconds
     * The actual expiration time will be: baseExpiration + random(0, randomOffsetSeconds)
     * This helps prevent cache avalanche by distributing expiration times
     * Default: 60 seconds (1 minute)
     */
    private long randomOffsetSeconds = 60;

    /**
     * Enable null value caching to prevent cache penetration
     * Default: true
     */
    private boolean cacheNullValues = true;

    /**
     * Expiration time for null values (shorter than normal values)
     * Default: "5m" (5 minutes)
     */
    private String nullValueExpireAfterWrite = "5m";

    /**
     * Cache warming strategy: preload data on startup
     * Default: false
     */
    private boolean enableWarmup = false;

    /**
     * Cache priority level (1-10, higher is more important)
     * Used for cache eviction priority
     * Default: 5
     */
    private int priority = 5;
}
