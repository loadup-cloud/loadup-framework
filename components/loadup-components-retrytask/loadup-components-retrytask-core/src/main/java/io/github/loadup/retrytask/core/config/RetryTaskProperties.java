package io.github.loadup.retrytask.core.config;

/*-
 * #%L
 * Loadup Components Retrytask Core
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

import io.github.loadup.retrytask.facade.enums.DbType;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
