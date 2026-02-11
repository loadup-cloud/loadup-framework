package io.github.loadup.retrytask.core.config;

import io.github.loadup.retrytask.facade.enums.DbType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Configuration properties for the retry task module.
 */
@Data
@ConfigurationProperties(prefix = "loadup.retrytask")
public class RetryTaskProperties {

    private Map<String, BizTypeConfig> bizTypes;

    /**
     * Table prefix for retry task table. Default is empty (table name: retry_task).
     * If set to "loadup_", table name becomes "loadup_retry_task".
     */
    private String tablePrefix = "";

    /**
     * Database type. Default is mysql.
     * Supported values: mysql, pgsql, oracle.
     */
    private DbType dbType = DbType.MYSQL;

    @Data
    public static class BizTypeConfig {

        private String strategy = "fixed";

        private int maxRetryCount = 10;

        private String notifier = "log";

        private String priority = "L"; // Default priority is Low

        private boolean executeImmediately = false; // Whether to execute immediately by default

        private boolean waitResult = false; // Whether to wait for result by default
    }
}
