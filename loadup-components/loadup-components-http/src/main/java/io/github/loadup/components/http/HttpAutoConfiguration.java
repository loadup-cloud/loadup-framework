/*
 * #%L
 * LoadUp Http
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
package io.github.loadup.components.http;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

/** Preserves Boot's RestClient customizers and shared Jackson conventions. */
@AutoConfiguration(
        afterName = {
            "org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration",
            "org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration"
        })
@ConditionalOnProperty(prefix = "loadup.http", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(HttpProperties.class)
public class HttpAutoConfiguration {
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public HttpTemplate httpTemplate(
            HttpProperties properties,
            ObjectProvider<RestClient.Builder> builders,
            ObjectMapper mapper,
            ObjectProvider<HttpCredentialProvider> credentials,
            ObjectProvider<SslBundles> sslBundles,
            ObjectProvider<MeterRegistry> metrics) {
        return new HttpTemplate(
                properties,
                builders::getObject,
                mapper,
                credentials.getIfAvailable(),
                sslBundles.getIfAvailable(),
                metrics.getIfAvailable());
    }
}
