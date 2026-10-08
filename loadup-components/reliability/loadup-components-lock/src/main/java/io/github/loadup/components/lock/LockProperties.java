/*
 * #%L
 * LoadUp Lock
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
package io.github.loadup.components.lock;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/** Opt-in integration; Redis topology and credentials stay in native Redisson configuration. */
@ConfigurationProperties("loadup.lock")
public record LockProperties(
        boolean enabled,
        String namespace,
        Duration waitTimeout,
        LockOptions.LeaseMode leaseMode,
        Duration leaseTime,
        Resource redissonConfig) {
    public LockProperties {
        if (waitTimeout == null) waitTimeout = Duration.ofSeconds(3);
        if (leaseMode == null) leaseMode = LockOptions.LeaseMode.WATCHDOG;
    }

    public LockOptions options() {
        return new LockOptions(waitTimeout, leaseMode, leaseTime);
    }
}
