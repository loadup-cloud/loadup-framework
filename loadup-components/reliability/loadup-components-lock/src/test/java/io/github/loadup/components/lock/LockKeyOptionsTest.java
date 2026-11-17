/*
 * Copyright (C) 2025-2026 LoadUp Cloud
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
package io.github.loadup.components.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class LockKeyOptionsTest {
    @Test
    void encodedSegmentsAndNamespacePreventCollisions() {
        var key = LockKey.of("payment", "a:b");
        assertThat(key.redisName("app"))
                .isNotEqualTo(LockKey.of("payment:a", "b").redisName("app"));
        assertThat(key.redisName("app")).isNotEqualTo(key.redisName("other"));
        assertThat(LockKey.of("payment", "{order}").redisName("app")).doesNotContain("{", "}");
        assertThat(key.toString()).doesNotContain("a:b", "order");
        assertThatIllegalArgumentException().isThrownBy(() -> LockKey.of(" payment", "order"));
        assertThatIllegalArgumentException().isThrownBy(() -> LockKey.of("payment", " order"));
    }

    @Test
    void rejectsInvalidOrAmbiguousDurations() {
        assertThatIllegalArgumentException().isThrownBy(() -> LockOptions.watchdog(Duration.ofMillis(-1)));
        assertThatIllegalArgumentException().isThrownBy(() -> LockOptions.watchdog(Duration.ofNanos(1)));
        assertThatIllegalArgumentException().isThrownBy(() -> LockOptions.fixed(Duration.ZERO, Duration.ZERO));
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () -> new LockOptions(Duration.ZERO, LockOptions.LeaseMode.WATCHDOG, Duration.ofSeconds(1)));
        assertThatIllegalArgumentException().isThrownBy(() -> LockOptions.watchdog(Duration.ofSeconds(Long.MAX_VALUE)));
        assertThat(LockOptions.watchdog(Duration.ZERO).waitTimeout()).isZero();
    }
}
