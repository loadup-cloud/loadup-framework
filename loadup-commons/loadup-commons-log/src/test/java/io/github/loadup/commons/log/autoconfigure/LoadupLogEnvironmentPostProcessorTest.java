/*-
 * #%L
 * Loadup Common Log
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
package io.github.loadup.commons.log.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.commons.log.LogContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class LoadupLogEnvironmentPostProcessorTest {

    private final LoadupLogEnvironmentPostProcessor processor = new LoadupLogEnvironmentPostProcessor();

    @Test
    void addsDefaultConsolePattern() {
        MockEnvironment environment = new MockEnvironment();

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("logging.pattern.console")).isEqualTo(LogContext.DEFAULT_CONSOLE_PATTERN);
    }

    @Test
    void applicationPatternHasHigherPriorityThanDefault() {
        MockEnvironment environment = new MockEnvironment().withProperty("logging.pattern.console", "custom");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("logging.pattern.console")).isEqualTo("custom");
    }

    @Test
    void canDisableTraceFieldsInDefaultPattern() {
        MockEnvironment environment = new MockEnvironment().withProperty("loadup.log.include-trace-context", "false");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("logging.pattern.console"))
                .isEqualTo(LogContext.DEFAULT_CONSOLE_PATTERN_WITHOUT_TRACE);
    }

    @Test
    void disabledLoggingDoesNotAddDefaults() {
        MockEnvironment environment = new MockEnvironment().withProperty("loadup.log.enabled", "false");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("logging.pattern.console")).isNull();
    }
}
