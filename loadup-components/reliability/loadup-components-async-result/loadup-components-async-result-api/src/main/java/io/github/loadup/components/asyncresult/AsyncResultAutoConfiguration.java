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
package io.github.loadup.components.asyncresult;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@AutoConfiguration(
        afterName = {
            "io.github.loadup.components.asyncresult.local.LocalAsyncResultAutoConfiguration",
            "io.github.loadup.components.asyncresult.redis.RedisAsyncResultAutoConfiguration"
        })
@ConditionalOnProperty(prefix = "loadup.components.async-result", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AsyncResultProperties.class)
public class AsyncResultAutoConfiguration {
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(AsyncResultTemplate.class)
    @ConditionalOnSingleCandidate(AsyncResultProvider.class)
    public AsyncResultTemplate asyncResultTemplate(
            AsyncResultProvider provider,
            AsyncResultProperties properties,
            ObjectProvider<ObjectMapper> mapper,
            ObjectProvider<MeterRegistry> metrics) {
        return new DefaultAsyncResultTemplate(
                provider,
                properties,
                mapper.getIfAvailable(() -> JsonMapper.builder().build()),
                metrics.getIfAvailable());
    }
}
