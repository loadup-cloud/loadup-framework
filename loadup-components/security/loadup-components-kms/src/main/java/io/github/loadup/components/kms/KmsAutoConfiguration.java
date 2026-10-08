/*
 * #%L
 * LoadUp Kms
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
package io.github.loadup.components.kms;

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

/** Opt-in Transit integration with distinct runtime and management identities. */
@AutoConfiguration(
        afterName = {
            "org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration",
            "org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration"
        })
@ConditionalOnProperty(prefix = "loadup.kms", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(KmsProperties.class)
public class KmsAutoConfiguration {
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(KmsTemplate.class)
    public OpenBaoKmsTemplate kmsTemplate(
            KmsProperties properties,
            ObjectProvider<KmsTokenProvider> tokens,
            ObjectProvider<RestClient.Builder> builders,
            ObjectMapper mapper,
            ObjectProvider<SslBundles> ssl,
            ObjectProvider<MeterRegistry> metrics) {
        KmsTokenProvider provider = tokens.getIfAvailable(() -> () -> System.getenv(properties.tokenEnv()));
        return new OpenBaoKmsTemplate(new OpenBaoTransitClient(
                properties,
                provider,
                false,
                builders::getObject,
                mapper,
                ssl.getIfAvailable(),
                metrics.getIfAvailable()));
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(KmsKeyManager.class)
    @ConditionalOnProperty(prefix = "loadup.kms", name = "management-enabled", havingValue = "true")
    public OpenBaoKeyManager kmsKeyManager(
            KmsProperties properties,
            ObjectProvider<KmsManagementTokenProvider> tokens,
            ObjectProvider<RestClient.Builder> builders,
            ObjectMapper mapper,
            ObjectProvider<SslBundles> ssl,
            ObjectProvider<MeterRegistry> metrics) {
        KmsManagementTokenProvider provider =
                tokens.getIfAvailable(() -> () -> System.getenv(properties.managementTokenEnv()));
        return new OpenBaoKeyManager(new OpenBaoTransitClient(
                properties,
                provider::token,
                true,
                builders::getObject,
                mapper,
                ssl.getIfAvailable(),
                metrics.getIfAvailable()));
    }
}
