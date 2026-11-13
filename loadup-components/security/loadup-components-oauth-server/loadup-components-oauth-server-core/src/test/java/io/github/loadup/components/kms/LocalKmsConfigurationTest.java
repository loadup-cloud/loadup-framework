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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Arrays;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.env.MockEnvironment;

class LocalKmsConfigurationTest {
    private static final String STORE_PASSWORD = "synthetic-store-password";
    private static final String KEY_PASSWORD = "synthetic-entry-password";

    @TempDir
    static Path directory;

    private static Path keyStore;

    @BeforeAll
    static void createKeyStore() throws Exception {
        keyStore = directory.resolve("keys.jceks");
        var store = KeyStore.getInstance("JCEKS");
        store.load(null, STORE_PASSWORD.toCharArray());
        store.setEntry(
                "aes",
                new KeyStore.SecretKeyEntry(new SecretKeySpec(new byte[32], "AES")),
                new KeyStore.PasswordProtection(KEY_PASSWORD.toCharArray()));
        try (var output = Files.newOutputStream(keyStore)) {
            store.store(output, STORE_PASSWORD.toCharArray());
        }
    }

    @Test
    void configuredStoreTypeAndDistinctEntryPasswordLoadUsableKeys() {
        configuredRunner().run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .hasSingleBean(LocalKmsKeyStore.class)
                    .hasSingleBean(LocalKmsTemplate.class);
            var properties = context.getBean(LocalKmsProperties.class);
            assertThat(properties.getKeyStoreLocation())
                    .isEqualTo(keyStore.toUri().toString());
            assertThat(properties.getKeyStoreType()).isEqualTo("JCEKS");
            assertThat(properties.getKeyStorePasswordEnv()).isEqualTo("COVERAGE_STORE_PASSWORD");
            assertThat(properties.getKeyPasswordEnv()).isEqualTo("COVERAGE_KEY_PASSWORD");
            var kms = context.getBean(KmsTemplate.class);
            var key = KmsKeyRef.latest("aes");
            byte[] plaintext = {1, 2, 3};
            assertThat(kms.decrypt(key, kms.encrypt(key, plaintext))).containsExactly(plaintext);
        });
    }

    @Test
    void absentLocationAndInvalidStoreTypeFailAtStartup() {
        baseRunner().withPropertyValues("loadup.kms.enabled=true").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasStackTraceContaining("key-store-location");
        });
        configuredRunner()
                .withPropertyValues("loadup.kms.local.key-store-type=NOT_A_KEYSTORE")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasStackTraceContaining("Cannot load local KMS KeyStore");
                });
    }

    @Test
    void missingOrEmptyStoreAndEntryPasswordsFailBeforeUse() {
        for (String override : new String[] {
            "loadup.kms.local.key-store-password-env=COVERAGE_UNSET_PASSWORD_82C39",
            "COVERAGE_STORE_PASSWORD=",
            "loadup.kms.local.key-password-env=COVERAGE_UNSET_PASSWORD_82C39",
            "COVERAGE_KEY_PASSWORD="
        }) {
            configuredRunner().withPropertyValues(override).run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                        .hasRootCauseInstanceOf(IllegalArgumentException.class)
                        .hasStackTraceContaining("password environment property is missing");
            });
        }
    }

    @Test
    void passwordPropertyNamesMustFollowTheEnvironmentNamingContract() {
        var configuration = new LocalKmsAutoConfiguration();
        var properties = new LocalKmsProperties();
        properties.setKeyStoreLocation(keyStore.toUri().toString());
        for (String name : Arrays.asList(null, "", "invalid-name")) {
            properties.setKeyStorePasswordEnv(name);
            assertThatThrownBy(() -> configuration.localKmsKeyStore(
                            properties, new MockEnvironment(), new DefaultResourceLoader()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Local KMS password environment-property name is invalid");
        }
    }

    @Test
    void localBinderRequiresOptInAndHonorsOtherBindersAndCustomTemplates() {
        for (String[] properties : new String[][] {
            {}, {"loadup.kms.enabled=false"}, {"loadup.kms.enabled=true", "loadup.kms.binder=openbao"}
        }) {
            baseRunner()
                    .withPropertyValues(properties)
                    .run(context -> assertThat(context)
                            .hasNotFailed()
                            .doesNotHaveBean(KmsTemplate.class)
                            .doesNotHaveBean(LocalKmsKeyStore.class));
        }
        var custom = mock(KmsTemplate.class);
        baseRunner()
                .withPropertyValues("loadup.kms.enabled=true")
                .withBean(KmsTemplate.class, () -> custom)
                .run(context -> {
                    assertThat(context).hasNotFailed().doesNotHaveBean(LocalKmsKeyStore.class);
                    assertThat(context.getBean(KmsTemplate.class)).isSameAs(custom);
                });
    }

    private static ApplicationContextRunner baseRunner() {
        return new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(LocalKmsAutoConfiguration.class));
    }

    private static ApplicationContextRunner configuredRunner() {
        return baseRunner()
                .withPropertyValues(
                        "loadup.kms.enabled=true",
                        "loadup.kms.binder=local",
                        "loadup.kms.local.key-store-location=" + keyStore.toUri(),
                        "loadup.kms.local.key-store-type=JCEKS",
                        "loadup.kms.local.key-store-password-env=COVERAGE_STORE_PASSWORD",
                        "loadup.kms.local.key-password-env=COVERAGE_KEY_PASSWORD",
                        "COVERAGE_STORE_PASSWORD=" + STORE_PASSWORD,
                        "COVERAGE_KEY_PASSWORD=" + KEY_PASSWORD);
    }
}
