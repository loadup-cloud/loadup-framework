/*-
 * #%L
 * Loadup Resilience4j API
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
package io.github.loadup.components.resilience4j;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Top-level LoadUp resilience4j configuration ({@code loadup.resilience4j.*}).
 *
 * <p>{@code enabled} switches the whole component; {@code binder-type} selects the backend.
 * Only the in-memory {@code core} binder exists today; a Redis binder is the planned extension
 * point for distributed circuit breaker / rate limiter state.
 */
@ConfigurationProperties(prefix = "loadup.resilience4j")
public class Resilience4jProperties {

    private boolean enabled = true;

    private String binderType = "core";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBinderType() {
        return binderType;
    }

    public void setBinderType(String binderType) {
        this.binderType = binderType;
    }
}
