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

import io.github.loadup.commons.log.LogContext;
import java.util.Map;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/** Adds logging defaults while preserving application-provided logging settings. */
public class LoadupLogEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "loadupLogDefaults";
    private static final String ENABLED_PROPERTY = "loadup.log.enabled";
    private static final String INCLUDE_TRACE_PROPERTY = "loadup.log.include-trace-context";
    private static final String PATTERN_PROPERTY = "loadup.log.console-pattern";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.getProperty(ENABLED_PROPERTY, Boolean.class, true)) {
            return;
        }

        boolean includeTrace = environment.getProperty(INCLUDE_TRACE_PROPERTY, Boolean.class, true);
        String defaultPattern =
                includeTrace ? LogContext.DEFAULT_CONSOLE_PATTERN : LogContext.DEFAULT_CONSOLE_PATTERN_WITHOUT_TRACE;
        String pattern = environment.getProperty(PATTERN_PROPERTY, defaultPattern);
        environment
                .getPropertySources()
                .addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, Map.of("logging.pattern.console", pattern)));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
