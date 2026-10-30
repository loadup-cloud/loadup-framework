/*
 * #%L
 * LoadUp Components DelayedTask
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
package io.github.loadup.components.delayedtask;

import io.github.loadup.commons.error.CommonException;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loadup.delayedtask")
public class DelayedTaskProperties {
    private int batchSize = 100;
    private int maxAttempts = 5;
    private Duration lease = Duration.ofMinutes(5);
    private Duration retryDelay = Duration.ofMinutes(1);

    private static void validate(Duration value) {
        if (value == null || value.compareTo(Duration.ofSeconds(1)) < 0 || value.compareTo(Duration.ofDays(30)) > 0) {
            invalid();
        }
    }

    private static void invalid() {
        throw new CommonException(DelayedTaskResultCodeEnum.INVALID_ARGUMENT);
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int value) {
        if (value < 1 || value > 1000) {
            invalid();
        }
        batchSize = value;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int value) {
        if (value < 1 || value > 1000) {
            invalid();
        }
        maxAttempts = value;
    }

    public Duration getLease() {
        return lease;
    }

    public void setLease(Duration value) {
        validate(value);
        lease = value;
    }

    public Duration getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(Duration value) {
        validate(value);
        retryDelay = value;
    }
}
