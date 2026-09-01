package io.github.loadup.components.authorization;

/*-
 * #%L
 * LoadUp Components Authorization
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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LoadUp authorization facade configuration ({@code loadup.authorization.*}).
 */
@ConfigurationProperties(prefix = "loadup.authorization")
public class AuthorizationProperties {

    /** Master switch for the authorization auto-configuration. */
    private boolean enabled = true;

    /**
     * Whether to register the default permissive, stateless {@code SecurityFilterChain}.
     *
     * <p>Set to {@code false} when the application defines its own filter chain.
     */
    private boolean defaultSecurityFilterChain = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDefaultSecurityFilterChain() {
        return defaultSecurityFilterChain;
    }

    public void setDefaultSecurityFilterChain(boolean defaultSecurityFilterChain) {
        this.defaultSecurityFilterChain = defaultSecurityFilterChain;
    }
}
