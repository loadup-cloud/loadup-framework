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
