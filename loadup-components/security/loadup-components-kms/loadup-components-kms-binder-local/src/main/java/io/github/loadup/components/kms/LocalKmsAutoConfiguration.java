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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/** Enables local KMS when the application supplies a KeyStore and its entry password. */
@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.kms", name = "enabled", havingValue = "true")
public class LocalKmsAutoConfiguration {
    @ConditionalOnProperty(prefix = "loadup.kms", name = "binder", havingValue = "local", matchIfMissing = true)
    @Bean
    @ConditionalOnMissingBean(KmsTemplate.class)
    LocalKmsTemplate localKmsTemplate(LocalKmsKeyStore keys) {
        return new LocalKmsTemplate(keys.store(), keys.password());
    }
}
