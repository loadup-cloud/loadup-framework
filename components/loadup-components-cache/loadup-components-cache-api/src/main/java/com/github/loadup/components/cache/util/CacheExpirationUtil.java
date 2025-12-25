/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.cache.util;

/*-
 * #%L
 * loadup-components-cache-api
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import com.github.loadup.components.cache.cfg.LoadUpCacheConfig;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Cache expiration utility to prevent cache avalanche and breakdown
 */
public class CacheExpirationUtil {

    /**
     * Calculate expiration duration with random offset to prevent cache avalanche
     *
     * @param baseDuration Base expiration duration
     * @param config       Cache configuration
     * @return Duration with random offset
     */
    public static Duration calculateExpirationWithRandomOffset(Duration baseDuration, LoadUpCacheConfig config) {
        if (config == null || !config.isEnableRandomExpiration()) {
            return baseDuration;
        }

        long baseSeconds = baseDuration.getSeconds();
        long randomOffset = config.getRandomOffsetSeconds();

        // Add random offset: 0 to randomOffsetSeconds
        long randomSeconds = ThreadLocalRandom.current().nextLong(0, randomOffset + 1);

        return Duration.ofSeconds(baseSeconds + randomSeconds);
    }

    /**
     * Calculate expiration duration with percentage-based random offset
     * This is more flexible for different base durations
     *
     * @param baseDuration     Base expiration duration
     * @param offsetPercentage Random offset percentage (0.0 to 1.0)
     * @return Duration with random offset
     */
    public static Duration calculateExpirationWithPercentageOffset(Duration baseDuration, double offsetPercentage) {
        long baseSeconds = baseDuration.getSeconds();
        long maxOffset = (long) (baseSeconds * offsetPercentage);

        if (maxOffset <= 0) {
            return baseDuration;
        }

        long randomSeconds = ThreadLocalRandom.current().nextLong(0, maxOffset + 1);
        return Duration.ofSeconds(baseSeconds + randomSeconds);
    }

    /**
     * Calculate expiration for null values (shorter duration)
     *
     * @param config Cache configuration
     * @return Duration for null value expiration
     */
    public static Duration calculateNullValueExpiration(LoadUpCacheConfig config) {
        if (config == null || config.getNullValueExpireAfterWrite() == null) {
            return Duration.ofMinutes(5); // Default 5 minutes
        }

        // Parse the duration string (e.g., "5m", "1h")
        return parseDuration(config.getNullValueExpireAfterWrite());
    }

    /**
     * Parse duration string to Duration object
     * Supports format: "30s", "5m", "1h", "2d"
     *
     * @param durationStr Duration string
     * @return Duration object
     */
    public static Duration parseDuration(String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            return Duration.ofMinutes(30); // Default 30 minutes
        }

        String trimmed = durationStr.trim().toLowerCase();
        long value;
        char unit;

        try {
            value = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
            unit = trimmed.charAt(trimmed.length() - 1);
        } catch (Exception e) {
            return Duration.ofMinutes(30); // Default on error
        }

        return switch (unit) {
            case 's' -> Duration.ofSeconds(value);
            case 'm' -> Duration.ofMinutes(value);
            case 'h' -> Duration.ofHours(value);
            case 'd' -> Duration.ofDays(value);
            default -> Duration.ofMinutes(30);
        };
    }

    /**
     * Calculate staggered expiration times for batch cache loading
     * Useful for cache warming scenarios
     *
     * @param baseDuration  Base expiration duration
     * @param index         Index of the item in batch
     * @param batchSize     Total batch size
     * @param staggerWindow Maximum stagger window duration
     * @return Staggered duration
     */
    public static Duration calculateStaggeredExpiration(Duration baseDuration, int index, int batchSize, Duration staggerWindow) {
        if (batchSize <= 1) {
            return baseDuration;
        }

        long baseSeconds = baseDuration.getSeconds();
        long staggerSeconds = staggerWindow.getSeconds();
        long offsetPerItem = staggerSeconds / batchSize;
        long itemOffset = offsetPerItem * index;

        return Duration.ofSeconds(baseSeconds + itemOffset);
    }
}

