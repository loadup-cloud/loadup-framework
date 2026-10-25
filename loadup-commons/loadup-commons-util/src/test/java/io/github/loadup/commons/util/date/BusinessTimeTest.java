/*
 * Copyright 2026 LoadUp Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.loadup.commons.util.date;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.loadup.commons.time.BusinessTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated("Changes the JVM default timezone")
class BusinessTimeTest {
    @Test
    void followsTheCurrentSystemZoneWithoutCachingARegion() {
        var original = TimeZone.getDefault();
        var instant = Instant.parse("2026-07-01T12:00:00Z");
        try {
            for (String name : new String[] {"UTC", "Europe/Berlin", "America/New_York"}) {
                TimeZone.setDefault(TimeZone.getTimeZone(name));
                var zone = ZoneId.systemDefault();
                var value = instant.atZone(zone).toOffsetDateTime();
                assertEquals(zone, BusinessTime.zone());
                assertEquals(value.toLocalDateTime(), BusinessTime.local(value));
                assertEquals(
                        instant,
                        BusinessTime.external(BusinessTime.local(value)).toInstant());
            }
        } finally {
            TimeZone.setDefault(original);
        }
    }
}
