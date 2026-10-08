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
import java.util.Objects;

/** Waiting and lease are separate policies. Fixed leases deliberately disable Redisson watchdog renewal. */
public record LockOptions(Duration waitTimeout, LeaseMode leaseMode, Duration leaseTime) {
    public enum LeaseMode {
        WATCHDOG,
        FIXED
    }

    public LockOptions {
        Objects.requireNonNull(leaseMode, "leaseMode");
        millis(waitTimeout, true, "waitTimeout");
        if (leaseMode == LeaseMode.WATCHDOG && leaseTime != null)
            throw new IllegalArgumentException("WATCHDOG must not specify leaseTime");
        if (leaseMode == LeaseMode.FIXED) millis(leaseTime, false, "leaseTime");
    }

    public static LockOptions watchdog(Duration waitTimeout) {
        return new LockOptions(waitTimeout, LeaseMode.WATCHDOG, null);
    }

    public static LockOptions fixed(Duration waitTimeout, Duration leaseTime) {
        return new LockOptions(waitTimeout, LeaseMode.FIXED, leaseTime);
    }

    static long millis(Duration value, boolean allowZero, String field) {
        Objects.requireNonNull(value, field);
        try {
            long millis = value.toMillis();
            if (value.isNegative() || (!allowZero && millis == 0) || (!value.isZero() && millis == 0))
                throw new IllegalArgumentException(
                        field + " must be " + (allowZero ? "zero or " : "") + "at least one millisecond");
            return millis;
        } catch (ArithmeticException overflow) {
            throw new IllegalArgumentException(field + " exceeds the supported millisecond range", overflow);
        }
    }
}
