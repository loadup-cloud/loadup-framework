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
package io.github.loadup.components.runtime.time;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ClockAutoConfigurationTest {
    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(ClockAutoConfiguration.class));

    @Test
    void providesASystemClockWhenTheApplicationDoesNotDefineOne() {
        runner.run(context -> assertNotNull(context.getBean(Clock.class)));
    }

    @Test
    void backsOffWhenTheApplicationDefinesItsOwnClock() {
        var fixed = Clock.fixed(Instant.parse("2026-10-23T10:11:12Z"), ZoneOffset.UTC);
        runner.withBean(Clock.class, () -> fixed).run(context -> assertSame(fixed, context.getBean(Clock.class)));
    }
}
