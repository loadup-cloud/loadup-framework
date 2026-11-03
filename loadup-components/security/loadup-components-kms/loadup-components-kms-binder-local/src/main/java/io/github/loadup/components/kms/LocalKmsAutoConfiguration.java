/*
 * Copyright (C) 2025-2026 LoadUp Cloud
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

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.kms", name = "enabled", havingValue = "true")
@ConditionalOnMissingBean(KmsTemplate.class)
@EnableConfigurationProperties(LocalKmsProperties.class)
public class LocalKmsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(LocalKmsKeyStore.class)
    @ConditionalOnProperty(prefix = "loadup.kms", name = "binder", havingValue = "local", matchIfMissing = true)
    LocalKmsKeyStore localKmsKeyStore(
            LocalKmsProperties properties, Environment environment, ResourceLoader resources) {
        if (!StringUtils.hasText(properties.getKeyStoreLocation())) {
            throw new IllegalArgumentException(
                    "loadup.kms.local.key-store-location or LocalKmsKeyStore bean is required");
        }
        char[] storePassword = password(environment, properties.getKeyStorePasswordEnv());
        char[] keyPassword = null;
        try {
            keyPassword = StringUtils.hasText(properties.getKeyPasswordEnv())
                    ? password(environment, properties.getKeyPasswordEnv())
                    : storePassword.clone();
            var store = KeyStore.getInstance(properties.getKeyStoreType());
            try (var input =
                    resources.getResource(properties.getKeyStoreLocation()).getInputStream()) {
                store.load(input, storePassword);
            }
            return new LocalKmsKeyStore(store, keyPassword);
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException failure) {
            throw new IllegalStateException(
                    "Cannot load local KMS KeyStore; check location, type and credentials", failure);
        } finally {
            Arrays.fill(storePassword, '\0');
            if (keyPassword != null) {
                Arrays.fill(keyPassword, '\0');
            }
        }
    }

    private static char[] password(Environment environment, String name) {
        if (name == null || !name.matches("[A-Z][A-Z0-9_]*")) {
            throw new IllegalArgumentException("Local KMS password environment-property name is invalid");
        }
        String value = environment.getProperty(name);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Required local KMS password environment property is missing");
        }
        return value.toCharArray();
    }

    @ConditionalOnProperty(prefix = "loadup.kms", name = "binder", havingValue = "local", matchIfMissing = true)
    @Bean
    @ConditionalOnMissingBean(KmsTemplate.class)
    LocalKmsTemplate localKmsTemplate(LocalKmsKeyStore keys) {
        return new LocalKmsTemplate(keys.store(), keys.password());
    }
}
