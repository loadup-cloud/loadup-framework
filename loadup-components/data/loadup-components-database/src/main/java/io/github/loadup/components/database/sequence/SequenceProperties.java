/*
 * #%L
 * Loadup Components Database
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
package io.github.loadup.components.database.sequence;

import io.github.loadup.commons.error.CommonException;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loadup.database.sequence")
public class SequenceProperties {
    private boolean enabled;
    private int step = 10_000, maxScopes = 1024, prefetchThreads = 2, queueCapacity = 256;
    private Duration waitTimeout = Duration.ofSeconds(3);

    public void validate() {
        if (step < 1
                || step > 1_000_000
                || maxScopes < 1
                || prefetchThreads < 1
                || queueCapacity < 1
                || waitTimeout == null
                || waitTimeout.isNegative()
                || waitTimeout.isZero()
                || waitTimeout.compareTo(Duration.ofMinutes(1)) > 0)
            throw new CommonException(DatabaseResultCodeEnum.INVALID_INPUT);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean v) {
        enabled = v;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int v) {
        step = v;
    }

    public int getMaxScopes() {
        return maxScopes;
    }

    public void setMaxScopes(int v) {
        maxScopes = v;
    }

    public int getPrefetchThreads() {
        return prefetchThreads;
    }

    public void setPrefetchThreads(int v) {
        prefetchThreads = v;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int v) {
        queueCapacity = v;
    }

    public Duration getWaitTimeout() {
        return waitTimeout;
    }

    public void setWaitTimeout(Duration v) {
        waitTimeout = v;
    }
}
