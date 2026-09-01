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
package io.github.loadup.commons.log;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.log")
public class LoadupLogProperties {

    private boolean enabled = true;

    private boolean includeTraceContext = true;

    private String consolePattern = LogContext.DEFAULT_CONSOLE_PATTERN;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isIncludeTraceContext() {
        return includeTraceContext;
    }

    public void setIncludeTraceContext(boolean includeTraceContext) {
        this.includeTraceContext = includeTraceContext;
    }

    public String getConsolePattern() {
        return consolePattern;
    }

    public void setConsolePattern(String consolePattern) {
        this.consolePattern = consolePattern;
    }
}
