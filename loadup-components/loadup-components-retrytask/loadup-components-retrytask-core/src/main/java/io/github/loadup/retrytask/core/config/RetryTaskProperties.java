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

import io.github.loadup.commons.enums.DbType;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the retry task module.
 */
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

    public static class BizTypeConfig {

        private String strategy = "fixed";

        private int maxRetryCount = 10;

        private String notifier = "log";

        private String priority = "L"; // Default priority is Low

        private boolean executeImmediately = false; // Whether to execute immediately by default

        private boolean waitResult = false; // Whether to wait for result by default
    }

    public RetryTaskProperties(Map<String, BizTypeConfig> bizTypes, String tablePrefix, DbType dbType, String strategy, int maxRetryCount, String notifier, String priority, boolean executeImmediately, boolean waitResult) {
        this.bizTypes = bizTypes;
        this.tablePrefix = tablePrefix;
        this.dbType = dbType;
        this.strategy = strategy;
        this.maxRetryCount = maxRetryCount;
        this.notifier = notifier;
        this.priority = priority;
        this.executeImmediately = executeImmediately;
        this.waitResult = waitResult;
    }

    public RetryTaskProperties() {
    }

    public Map<String, BizTypeConfig> getBizTypes() {
        return this.bizTypes;
    }

    public String getTablePrefix() {
        return this.tablePrefix;
    }

    public DbType getDbType() {
        return this.dbType;
    }

    public String getStrategy() {
        return this.strategy;
    }

    public int getMaxRetryCount() {
        return this.maxRetryCount;
    }

    public String getNotifier() {
        return this.notifier;
    }

    public String getPriority() {
        return this.priority;
    }

    public boolean isExecuteImmediately() {
        return this.executeImmediately;
    }

    public boolean isWaitResult() {
        return this.waitResult;
    }

    public void setBizTypes(Map<String, BizTypeConfig> bizTypes) {
        this.bizTypes = bizTypes;
    }

    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public void setNotifier(String notifier) {
        this.notifier = notifier;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setExecuteImmediately(boolean executeImmediately) {
        this.executeImmediately = executeImmediately;
    }

    public void setWaitResult(boolean waitResult) {
        this.waitResult = waitResult;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(bizTypes, tablePrefix, dbType, strategy, maxRetryCount, notifier, priority, executeImmediately, waitResult);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetryTaskProperties other = (RetryTaskProperties) o;
        if (!java.util.Objects.equals(bizTypes, other.bizTypes)) return false;
        if (!java.util.Objects.equals(tablePrefix, other.tablePrefix)) return false;
        if (!java.util.Objects.equals(dbType, other.dbType)) return false;
        if (!java.util.Objects.equals(strategy, other.strategy)) return false;
        if (!java.util.Objects.equals(maxRetryCount, other.maxRetryCount)) return false;
        if (!java.util.Objects.equals(notifier, other.notifier)) return false;
        if (!java.util.Objects.equals(priority, other.priority)) return false;
        if (!java.util.Objects.equals(executeImmediately, other.executeImmediately)) return false;
        if (!java.util.Objects.equals(waitResult, other.waitResult)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "RetryTaskProperties(" + "bizTypes=" + bizTypes + ", " + "tablePrefix=" + tablePrefix + ", " + "dbType=" + dbType + ", " + "strategy=" + strategy + ", " + "maxRetryCount=" + maxRetryCount + ", " + "notifier=" + notifier + ", " + "priority=" + priority + ", " + "executeImmediately=" + executeImmediately + ", " + "waitResult=" + waitResult + ")";
    }
}
