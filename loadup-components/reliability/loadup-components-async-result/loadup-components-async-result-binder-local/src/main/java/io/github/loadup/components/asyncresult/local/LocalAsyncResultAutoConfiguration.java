/*
 * #%L
 * LoadUp Async Result
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
package io.github.loadup.components.asyncresult.local;

import io.github.loadup.components.asyncresult.AsyncResultProperties;
import io.github.loadup.components.asyncresult.AsyncResultProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(AsyncResultProperties.class)
@ConditionalOnProperty(
        prefix = "loadup.components.async-result",
        name = "binder-type",
        havingValue = "local",
        matchIfMissing = true)
public class LocalAsyncResultAutoConfiguration {
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(AsyncResultProvider.class)
    @ConditionalOnProperty(prefix = "loadup.components.async-result", name = "enabled", havingValue = "true")
    public LocalAsyncResultProvider localAsyncResultProvider(AsyncResultProperties properties) {
        return new LocalAsyncResultProvider(properties.maxRetained());
    }
}
