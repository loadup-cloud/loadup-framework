/*-
 * #%L
 * Loadup Components Retrytask Binder JobRunr
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

package io.github.loadup.retrytask.jobrunr;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LoadUp retry task configuration. Runtime engine behaviour (poll interval, worker count, global
 * retry default, retention) is delegated to the official {@code jobrunr.*} properties of the
 * JobRunr Spring Boot starter; these properties only enrich per-business-type defaults.
 */
@ConfigurationProperties(prefix = "loadup.retrytask")
public class RetryTaskProperties {

    /** Per-business-type overrides, keyed by bizType. */
    private final Map<String, BizTypeConfig> bizTypes = new HashMap<>();

    /**
     * Resolves the effective retry count for a business type.
     *
     * @param bizType the business type
     * @param requestOverride an optional request-level override
     * @return the retry count, or {@code null} to leave the JobRunr global default in charge
     */
    public Integer resolveMaxRetries(String bizType, Integer requestOverride) {
        if (requestOverride != null) {
            return requestOverride;
        }
        BizTypeConfig config = bizTypes.get(bizType);
        return config != null ? config.getMaxRetries() : null;
    }

    public Map<String, BizTypeConfig> getBizTypes() {
        return bizTypes;
    }

    /**
     * Per-business-type retry task configuration.
     */
    public static class BizTypeConfig {

        /** Retry count after the initial attempt; {@code null} falls back to the global default. */
        private Integer maxRetries;

        public Integer getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
        }
    }
}
