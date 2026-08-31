package io.github.loadup.components.cache.test;

/*-
 * #%L
 * Loadup Cache Component Test
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.loadup.components.cache.RandomExpiration;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class RandomExpirationTest {

    @Test
    void returnsBaseWhenJitterIsNullOrZero() {
        Duration base = Duration.ofSeconds(30);

        assertEquals(base, RandomExpiration.apply(base, null));
        assertEquals(base, RandomExpiration.apply(base, Duration.ZERO));
    }

    @Test
    void returnsNullWhenBaseIsNull() {
        assertNull(RandomExpiration.apply(null, Duration.ofSeconds(5)));
    }

    @Test
    void returnsValueWithinBaseAndBasePlusJitter() {
        Duration base = Duration.ofSeconds(10);
        Duration jitter = Duration.ofSeconds(3);

        for (int i = 0; i < 100; i++) {
            Duration result = RandomExpiration.apply(base, jitter);
            assertTrue(!result.isNegative() && result.compareTo(base) >= 0);
            assertTrue(result.compareTo(base.plus(jitter)) <= 0);
        }
    }
}
