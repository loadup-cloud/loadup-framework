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

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import javax.crypto.KeyGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class LocalKmsSecretManagerTest {
    @Test
    void writesOnlyCiphertextAndReadsTheLatestValueAfterRestart(@TempDir Path root) throws Exception {
        Path keyStore = root.resolve("keys.p12");
        Path directory = root.resolve("secrets");
        writeKeyStore(keyStore);
        var runner = new ApplicationContextRunner()
                .withConfiguration(
                        AutoConfigurations.of(LocalKmsAutoConfiguration.class, LocalKmsSecretAutoConfiguration.class))
                .withPropertyValues(
                        "loadup.kms.enabled=true",
                        "KMS_KEYSTORE_PASSWORD=fixture-password",
                        "loadup.kms.local.key-store-location=" + keyStore.toUri());
        runner.run(context ->
                assertThat(context).hasSingleBean(KmsTemplate.class).doesNotHaveBean(KmsSecretManager.class));
        var secretsRunner = runner.withPropertyValues(
                "loadup.kms.secrets-enabled=true",
                "loadup.kms.secret-key-alias=credentials",
                "loadup.kms.local.secret-directory=" + directory);
        secretsRunner.run(context -> {
            assertThat(context).hasSingleBean(KmsSecretManager.class);
            var secrets = context.getBean(KmsSecretManager.class);
            assertThatThrownBy(() -> secrets.update("missing-name", "no-create"))
                    .hasMessage("KMS operation failed: NOT_FOUND");
            secrets.create("payments-token", "原始凭据");
            assertThatThrownBy(() -> secrets.create("payments-token", "duplicate"))
                    .hasMessage("KMS operation failed: INVALID_REQUEST");
            assertThat(secrets.get("payments-token")).isEqualTo("原始凭据");
            secrets.update("payments-token", "更新凭据");
        });
        Path file;
        try (var files = Files.list(directory)) {
            file = files.filter(path -> path.toString().endsWith(".enc"))
                    .findFirst()
                    .orElseThrow();
        }
        String updatedCiphertext = Files.readString(file);
        assertThat(updatedCiphertext).startsWith("local:v1:").doesNotContain("原始凭据", "更新凭据", "payments-token");
        secretsRunner.run(context -> {
            var secrets = context.getBean(KmsSecretManager.class);
            assertThat(secrets.get("payments-token")).isEqualTo("更新凭据");
            secrets.create("different-name", "不同凭据");
            try (var files = Files.list(directory)) {
                var otherFile = files.filter(path -> path.toString().endsWith(".enc") && !path.equals(file))
                        .findFirst()
                        .orElseThrow();
                Files.writeString(otherFile, updatedCiphertext);
            }
            assertThatThrownBy(() -> secrets.get("different-name"))
                    .hasMessage("KMS operation failed: INVALID_RESPONSE");
        });
    }

    private static void writeKeyStore(Path path) throws Exception {
        char[] password = "fixture-password".toCharArray();
        var generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        var store = KeyStore.getInstance("PKCS12");
        store.load(null, password);
        store.setEntry(
                "credentials",
                new KeyStore.SecretKeyEntry(generator.generateKey()),
                new KeyStore.PasswordProtection(password));
        try (var output = Files.newOutputStream(path)) {
            store.store(output, password);
        }
    }
}
