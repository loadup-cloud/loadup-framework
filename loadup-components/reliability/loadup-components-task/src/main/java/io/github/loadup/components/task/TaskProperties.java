/*
 * Copyright (C) 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.task;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loadup.task")
public class TaskProperties {
    public enum DbType {
        PG,
        MYSQL
    }

    public enum Trigger {
        LOCAL,
        SCHEDULER
    }

    private DbType dbType = DbType.PG;
    private String tablePrefix = "loadup_";
    private String taskType;

    public DbType getDbType() {
        return dbType;
    }

    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String value) {
        taskType = value;
    }

    private boolean workerEnabled = true;
    private Trigger trigger = Trigger.LOCAL;
    private Duration pollInterval = Duration.ofMinutes(1);
    private Duration lease = Duration.ofMinutes(2);
    private Duration retryDelay = Duration.ofSeconds(5);
    private int maxAttempts = 8;
    private int batchSize = 10;

    private Map<String, Policy> types = new HashMap<>();

    public Map<String, Policy> getTypes() {
        return types;
    }

    public void setTypes(Map<String, Policy> value) {
        types = value == null ? Map.of() : Map.copyOf(value);
    }

    public Policy policy(String type) {
        Policy value = types.get(type);
        return value == null
                ? new Policy(maxAttempts, lease, retryDelay, true)
                : new Policy(
                        value.maxAttempts == null ? maxAttempts : value.maxAttempts,
                        value.lease == null ? lease : value.lease,
                        value.retryDelay == null ? retryDelay : value.retryDelay,
                        value.exponential == null || value.exponential);
    }

    public static class Policy {
        private Integer maxAttempts;
        private Duration lease;
        private Duration retryDelay;
        private Boolean exponential;

        public Policy() {}

        public Policy(int attempts, Duration lease, Duration delay, boolean exponential) {
            this.maxAttempts = attempts;
            this.lease = lease;
            this.retryDelay = delay;
            this.exponential = exponential;
        }

        public Integer getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(Integer value) {
            maxAttempts = value;
        }

        public Duration getLease() {
            return lease;
        }

        public void setLease(Duration value) {
            lease = value;
        }

        public Duration getRetryDelay() {
            return retryDelay;
        }

        public void setRetryDelay(Duration value) {
            retryDelay = value;
        }

        public boolean isExponential() {
            return Boolean.TRUE.equals(exponential);
        }

        public void setExponential(boolean value) {
            exponential = value;
        }

        long retryDelayMillis(int attempt) {
            long multiplier = isExponential() ? (1L << Math.min(20, Math.max(0, attempt - 1))) : 1L;
            return Math.min(Duration.ofDays(1).toMillis(), retryDelay.toMillis() * multiplier);
        }

        void validate() {
            if (maxAttempts < 1
                    || maxAttempts > 1000
                    || lease == null
                    || lease.toMillis() < 1
                    || lease.compareTo(Duration.ofDays(30)) > 0
                    || retryDelay == null
                    || retryDelay.toMillis() < 1
                    || retryDelay.compareTo(Duration.ofDays(1)) > 0) {
                throw new IllegalArgumentException("Invalid task type policy");
            }
        }
    }

    public void validate() {
        if (dbType == null || tablePrefix == null || !tablePrefix.matches("(?:[a-z][a-z0-9_]{0,48})?")) {
            throw new IllegalArgumentException("Invalid task database type or table prefix");
        }
        if (taskType != null) {
            TaskSubmission.required(taskType, "taskType", 128);
        }
        types.keySet().forEach(type -> {
            TaskSubmission.required(type, "taskType", 128);
            policy(type).validate();
        });
        if (trigger == null
                || pollInterval == null
                || pollInterval.toMillis() < 1
                || lease == null
                || lease.toMillis() < 1
                || lease.compareTo(Duration.ofDays(30)) > 0
                || retryDelay == null
                || retryDelay.toMillis() < 1
                || retryDelay.toMillis() > 86400000
                || maxAttempts < 1
                || maxAttempts > 1000
                || batchSize < 1
                || batchSize > 1000) {
            throw new IllegalArgumentException("Invalid task delivery settings");
        }
    }

    public boolean isWorkerEnabled() {
        return workerEnabled;
    }

    public void setWorkerEnabled(boolean value) {
        workerEnabled = value;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger value) {
        trigger = value;
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Duration value) {
        pollInterval = value;
    }

    public Duration getLease() {
        return lease;
    }

    public void setLease(Duration value) {
        lease = value;
    }

    public Duration getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(Duration value) {
        retryDelay = value;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int value) {
        maxAttempts = value;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int value) {
        batchSize = value;
    }
}
