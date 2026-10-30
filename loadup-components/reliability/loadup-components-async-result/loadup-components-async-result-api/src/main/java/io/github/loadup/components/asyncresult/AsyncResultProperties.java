/*
 * #%L
 * LoadUp Async Result
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
package io.github.loadup.components.asyncresult;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loadup.components.async-result")
public record AsyncResultProperties(
        boolean enabled,
        String binderType,
        String namespace,
        Duration waitTimeout,
        Duration maxWaitTimeout,
        int maxPending,
        Duration resultRetention,
        Duration fallbackPollInterval,
        int maxResultBytes,
        int maxRetained) {
    public AsyncResultProperties {
        binderType = binderType == null ? "local" : binderType;
        if (!binderType.equals("local") && !binderType.equals("redis")) {
            throw new IllegalArgumentException("Unknown async result binder");
        }
        if (enabled) {
            AsyncReplyKey.required(namespace, 128);
        }
        waitTimeout = waitTimeout == null ? Duration.ofSeconds(5) : waitTimeout;
        maxWaitTimeout = maxWaitTimeout == null ? Duration.ofSeconds(30) : maxWaitTimeout;
        resultRetention = resultRetention == null ? Duration.ofMinutes(15) : resultRetention;
        fallbackPollInterval = fallbackPollInterval == null ? Duration.ofMillis(500) : fallbackPollInterval;
        maxPending = maxPending == 0 ? 10000 : maxPending;
        maxResultBytes = maxResultBytes == 0 ? 65536 : maxResultBytes;
        maxRetained = maxRetained == 0 ? 100000 : maxRetained;
        if (waitTimeout.toMillis() < 1
                || maxWaitTimeout.compareTo(Duration.ofDays(1)) > 0
                || waitTimeout.compareTo(maxWaitTimeout) > 0
                || resultRetention.compareTo(maxWaitTimeout) < 0
                || resultRetention.compareTo(Duration.ofDays(7)) > 0
                || fallbackPollInterval.toMillis() < 1
                || maxPending < 1
                || maxResultBytes < 1
                || maxResultBytes > 1024 * 1024
                || maxRetained < maxPending) {
            throw new IllegalArgumentException("Invalid async result limits");
        }
    }

    @Override
    public String toString() {
        return io.github.loadup.commons.json.ToStringAsJson.reflectionToString(this);
    }
}
