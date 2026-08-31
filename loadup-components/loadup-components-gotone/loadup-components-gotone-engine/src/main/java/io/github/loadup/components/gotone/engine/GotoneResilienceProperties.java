/*-
 * #%L
 * Loadup Gotone Engine
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
package io.github.loadup.components.gotone.engine;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Gotone resilience configuration ({@code loadup.gotone.resilience.*}).
 *
 * <p>When enabled and a resilience4j binder is present, every channel provider is wrapped
 * with a per-provider circuit breaker and retry. Instance names follow the
 * {@code gotone-<channelType>-<providerName>} convention and are configured with the standard
 * {@code resilience4j.*} properties.
 */
@ConfigurationProperties(prefix = "loadup.gotone.resilience")
public class GotoneResilienceProperties {

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
