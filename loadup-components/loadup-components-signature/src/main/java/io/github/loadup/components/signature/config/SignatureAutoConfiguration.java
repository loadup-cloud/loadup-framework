package io.github.loadup.components.signature.config;

/*-
 * #%L
 * LoadUp Components :: Signature
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

import io.github.loadup.components.signature.properties.SignatureProperties;
import io.github.loadup.components.signature.service.DigestService;
import io.github.loadup.components.signature.service.KeyPairService;
import io.github.loadup.components.signature.service.SignatureService;
import io.github.loadup.components.signature.service.impl.DigestServiceImpl;
import io.github.loadup.components.signature.service.impl.KeyPairServiceImpl;
import io.github.loadup.components.signature.service.impl.SignatureServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that wires the signature services with explicit beans.
 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "loadup.components.signature",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(SignatureProperties.class)
public class SignatureAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KeyPairService keyPairService(SignatureProperties properties) {
        return new KeyPairServiceImpl(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SignatureService signatureService(KeyPairService keyPairService) {
        return new SignatureServiceImpl(keyPairService);
    }

    @Bean
    @ConditionalOnMissingBean
    public DigestService digestService() {
        return new DigestServiceImpl();
    }
}
