/*
 * #%L
 * LoadUp Outbox
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
package io.github.loadup.components.outbox;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Delivery settings, validated before starting the worker. */
@ConfigurationProperties("loadup.outbox")
public class OutboxProperties {
    private boolean workerEnabled = true;
    private Duration pollInterval = Duration.ofSeconds(1);
    private Duration lease = Duration.ofMinutes(2);
    private Duration retryDelay = Duration.ofSeconds(5);
    private int maxAttempts = 8;
    private int batchSize = 20;

    public void validate() {
        if (pollInterval == null
                || pollInterval.toMillis() < 1
                || lease == null
                || lease.toMillis() < 1
                || retryDelay == null
                || retryDelay.toMillis() < 1
                || retryDelay.toMillis() > 86400000
                || maxAttempts < 1
                || maxAttempts > 100
                || batchSize < 1
                || batchSize > 1000) {
            throw new IllegalArgumentException("Invalid outbox delivery settings");
        }
    }

    public boolean isWorkerEnabled() {
        return workerEnabled;
    }

    public void setWorkerEnabled(boolean value) {
        workerEnabled = value;
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
