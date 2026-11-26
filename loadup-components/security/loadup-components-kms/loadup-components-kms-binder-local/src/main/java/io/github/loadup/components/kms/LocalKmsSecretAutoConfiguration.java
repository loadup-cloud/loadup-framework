/*
 * Copyright (C) 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.kms;

import io.github.loadup.components.kms.keystore.KeyStoreKmsSecretManager;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@AutoConfiguration(after = LocalKmsAutoConfiguration.class)
@ConditionalOnProperty(
        prefix = "loadup.kms",
        name = {"enabled", "secrets-enabled"},
        havingValue = "true")
@ConditionalOnProperty(prefix = "loadup.kms", name = "binder", havingValue = "local", matchIfMissing = true)
public class LocalKmsSecretAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(KmsSecretManager.class)
    KmsSecretManager localKmsSecretManager(
            KmsTemplate kms,
            @Value("${loadup.kms.secret-key-alias:}") String keyAlias,
            @Value("${loadup.kms.local.secret-directory:}") String directory) {
        if (!StringUtils.hasText(directory)) {
            throw new IllegalArgumentException("loadup.kms.local.secret-directory is required");
        }
        return new KeyStoreKmsSecretManager(kms, keyAlias, new LocalKmsSecretFiles(Path.of(directory)));
    }
}
