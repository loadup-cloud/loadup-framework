package io.github.loadup.components.cache.test;

/*-
 * #%L
 * Loadup Cache Component Test
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
