/*-
 * #%L
 * Loadup Components Retrytask Binder JobRunr
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
