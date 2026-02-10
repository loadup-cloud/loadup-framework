package io.github.loadup.retrytask.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Configuration properties for the retry task module.
 */
@Data
@ConfigurationProperties(prefix = "loadup.retrytask")
public class RetryTaskProperties {

    private Map<String, BizTypeConfig> bizTypes;

    @Data
    public static class BizTypeConfig {

        private String strategy = "fixed";

        private int maxRetryCount = 10;

        private String notifier = "log";

        private String priority = "L"; // Default priority is Low
    }
}
