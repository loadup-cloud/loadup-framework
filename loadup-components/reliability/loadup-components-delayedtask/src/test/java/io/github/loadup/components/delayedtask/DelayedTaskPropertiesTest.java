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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class DelayedTaskPropertiesTest {
    @Test
    void boundsAreValidated() {
        var p = new DelayedTaskProperties();
        p.setBatchSize(1);
        p.setBatchSize(1000);
        p.setMaxAttempts(1);
        p.setMaxAttempts(1000);
        p.setLease(Duration.ofSeconds(1));
        p.setRetryDelay(Duration.ofDays(30));
        assertThat(p.getBatchSize()).isEqualTo(1000);
        assertThat(p.getMaxAttempts()).isEqualTo(1000);
        assertThat(p.getLease()).isEqualTo(Duration.ofSeconds(1));
        assertThat(p.getRetryDelay()).isEqualTo(Duration.ofDays(30));
        for (int n : new int[] {0, 1001}) {
            assertThatThrownBy(() -> p.setBatchSize(n))
                    .isInstanceOf(io.github.loadup.commons.error.CommonException.class);
            assertThatThrownBy(() -> p.setMaxAttempts(n))
                    .isInstanceOf(io.github.loadup.commons.error.CommonException.class);
        }
        for (Duration duration : java.util.Arrays.asList(null, Duration.ZERO, Duration.ofDays(31))) {
            assertThatThrownBy(() -> p.setLease(duration))
                    .isInstanceOf(io.github.loadup.commons.error.CommonException.class);
            assertThatThrownBy(() -> p.setRetryDelay(duration))
                    .isInstanceOf(io.github.loadup.commons.error.CommonException.class);
        }
    }
}
