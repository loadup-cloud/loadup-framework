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
package io.github.loadup.components.kms.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.kms.KmsException;
import io.github.loadup.components.kms.KmsKeyRef;
import io.github.loadup.components.kms.KmsSecretManager;
import io.github.loadup.components.kms.KmsSignatureAlgorithm;
import io.github.loadup.components.kms.KmsTemplate;
import io.github.loadup.components.kms.database.autoconfigure.DatabaseKmsAutoConfiguration;
import io.github.loadup.components.kms.keystore.KeyStoreKmsTemplate;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.crypto.KeyGenerator;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.ClassUtils;

@SpringBootTest(classes = DatabaseKmsKeyFilesIT.Application.class)
@EnableTestContainers(value = ContainerType.POSTGRESQL, reuse = false)
@Sql(
        scripts = {"classpath:db/schema/kms_key_file.sql", "classpath:db/schema/kms_secret.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class DatabaseKmsKeyFilesIT {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class Application {}

    private final DataSource dataSource;

    @Autowired
    DatabaseKmsKeyFilesIT(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Test
    void importsOnceAndReloadsThroughTheSelectedBinder() throws Exception {
        String id = UUID.randomUUID().toString();
        var files = new DatabaseKmsKeyFiles(dataSource);
        char[] storePassword = "fixture-store".toCharArray();
        char[] keyPassword = "fixture-entry".toCharArray();
        byte[] file = testFile(storePassword, keyPassword);
        assertThatThrownBy(() -> files.importFile(id, new ByteArrayInputStream(file), storePassword, storePassword))
                .isInstanceOf(KmsException.class);
        files.importFile(id, new ByteArrayInputStream(file), storePassword, keyPassword);
        assertThatThrownBy(() -> files.importFile(id, new ByteArrayInputStream(file), storePassword, keyPassword))
                .isInstanceOf(IllegalStateException.class);
        var ref = KmsKeyRef.latest("payments");
        byte[] message = {1, 2, 3};
        var encrypted = files.loadTemplate(id, storePassword, keyPassword).encrypt(ref, message);
        var runner = databaseContext()
                .withPropertyValues(
                        "loadup.kms.database.file-id=" + id,
                        "KMS_KEYSTORE_PASSWORD=fixture-store",
                        "loadup.kms.database.key-password-env=KMS_ENTRY_PASSWORD",
                        "KMS_ENTRY_PASSWORD=fixture-entry");
        runner.run(context -> {
            assertThat(context)
                    .hasSingleBean(KmsTemplate.class)
                    .doesNotHaveBean(DatabaseKmsKeyFiles.class)
                    .doesNotHaveBean(KmsSecretManager.class);
            assertThat(context.getBean(KmsTemplate.class)).isInstanceOf(KeyStoreKmsTemplate.class);
            assertThat(context.getBean(KmsTemplate.class).decrypt(ref, encrypted))
                    .containsExactly(message);
        });
        runner.withPropertyValues("loadup.kms.management-enabled=true").run(context -> {
            var manager = context.getBean(DatabaseKmsKeyFiles.class);
            assertThat(manager.retireFile(id)).isTrue();
            assertThat(manager.retireFile(id)).isFalse();
            assertThat(context.getBean(KmsTemplate.class).decrypt(ref, encrypted))
                    .containsExactly(message);
        });
        runner.run(context -> assertThat(context)
                .getFailure()
                .hasRootCauseInstanceOf(KmsException.class)
                .hasRootCauseMessage("KMS operation failed: NOT_FOUND"));
        assertThatThrownBy(() -> files.importFile(id, new ByteArrayInputStream(file), storePassword, keyPassword))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rotatedDatabaseSnapshotKeepsHistoricalVerificationAfterReload(@TempDir Path directory) throws Exception {
        String password = "fixture-store";
        char[] pass = password.toCharArray();
        var files = new DatabaseKmsKeyFiles(dataSource);
        var oldStore = rsaStore(directory.resolve("old.p12"), password);
        var newStore = rsaStore(directory.resolve("new.p12"), password);
        String firstId = UUID.randomUUID().toString();
        String nextId = UUID.randomUUID().toString();
        var alias = KmsKeyRef.latest("payments");
        var algorithm = KmsSignatureAlgorithm.RSA_SHA256_PKCS1;
        byte[] payload = {1, 2, 3};
        files.importFile(firstId, serialized(oldStore, pass), pass, pass);
        var first = files.loadTemplate(firstId, pass, pass);
        var fingerprint = first.currentSigningKey(alias.name()).fingerprint();
        var historical = first.sign(alias, algorithm, payload);

        newStore.setCertificateEntry("payments.history.previous", oldStore.getCertificate("payments"));
        files.importFile(nextId, serialized(newStore, pass), pass, pass);
        assertThat(files.retireFile(firstId)).isTrue();
        var restarted = new DatabaseKmsKeyFiles(dataSource).loadTemplate(nextId, pass, pass);
        assertThat(restarted.currentSigningKey(alias.name()).fingerprint()).isNotEqualTo(fingerprint);
        assertThat(restarted.verificationKeys(alias.name())).hasSize(2);
        assertThat(restarted.verify(alias.name(), fingerprint, algorithm, payload, historical.bytes()))
                .isTrue();
        assertThat(restarted.verify(alias.name(), fingerprint, algorithm, new byte[] {4}, historical.bytes()))
                .isFalse();
    }

    @Test
    void rejectsAnInvalidFileWithoutReservingItsId() throws Exception {
        String id = UUID.randomUUID().toString();
        char[] password = "fixture-password".toCharArray();
        var files = new DatabaseKmsKeyFiles(dataSource);
        byte[] invalidFile = {0x30, 0x00};
        assertThatThrownBy(() -> files.importFile(id, new ByteArrayInputStream(invalidFile), password, password))
                .isInstanceOf(KmsException.class)
                .hasMessage("KMS operation failed: INVALID_REQUEST");
        assertThatThrownBy(() -> files.loadTemplate(id, password, password))
                .isInstanceOf(KmsException.class)
                .hasMessage("KMS operation failed: NOT_FOUND");

        files.importFile(id, new ByteArrayInputStream(testFile(password, password)), password, password);
        assertThat(files.loadTemplate(id, password, password)
                        .metadata("payments")
                        .supportsEncryption())
                .isTrue();
    }

    @Test
    void persistsCiphertextOnlyAndReadsUpdatedSecretAfterRestart() throws Exception {
        String fileId = UUID.randomUUID().toString();
        char[] password = "fixture-password".toCharArray();
        byte[] file = testFile(password, password);
        new DatabaseKmsKeyFiles(dataSource).importFile(fileId, new ByteArrayInputStream(file), password, password);

        var runner = databaseContext()
                .withPropertyValues(
                        "loadup.kms.secrets-enabled=true",
                        "loadup.kms.secret-key-alias=payments",
                        "loadup.kms.database.file-id=" + fileId,
                        "KMS_KEYSTORE_PASSWORD=fixture-password");
        runner.run(context -> {
            assertThat(context).hasSingleBean(KmsSecretManager.class);
            var secrets = context.getBean(KmsSecretManager.class);
            assertThatThrownBy(() -> secrets.get("payments-token")).hasMessage("KMS operation failed: NOT_FOUND");
            assertThatThrownBy(() -> secrets.update("payments-token", "missing"))
                    .hasMessage("KMS operation failed: NOT_FOUND");
            secrets.create("payments-token", "原始凭据");
            assertThatThrownBy(() -> secrets.create("payments-token", "duplicate"))
                    .hasMessage("KMS operation failed: INVALID_REQUEST");
            assertThat(secrets.get("payments-token")).isEqualTo("原始凭据");
            secrets.update("payments-token", "更新凭据");
        });

        var jdbc = JdbcClient.create(dataSource);
        assertThat(jdbc.sql("SELECT id FROM kms_secret").query(String.class).single())
                .matches("[a-f0-9]{64}");
        assertThat(jdbc.sql("SELECT ciphertext FROM kms_secret")
                        .query(String.class)
                        .single())
                .startsWith("local:v1:")
                .doesNotContain("原始凭据", "更新凭据", "payments-token");

        runner.run(context -> assertThat(context.getBean(KmsSecretManager.class).get("payments-token"))
                .isEqualTo("更新凭据"));
    }

    private ApplicationContextRunner databaseContext() {
        // Contexts under test borrow connections from the pool owned by the outer test context.
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(DatabaseKmsAutoConfiguration.class))
                .withBean(DataSource.class, () -> new DelegatingDataSource(dataSource))
                .withPropertyValues("loadup.kms.enabled=true", "loadup.kms.binder=database");
    }

    private static KeyStore rsaStore(Path path, String password) throws Exception {
        var builder = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "keytool").toString(),
                "-genkeypair",
                "-alias",
                "payments",
                "-keyalg",
                "RSA",
                "-keysize",
                "2048",
                "-dname",
                "CN=KMS fixture",
                "-validity",
                "30",
                "-storetype",
                "PKCS12",
                "-keystore",
                path.toString(),
                "-storepass:env",
                "KMS_TEST_PASSWORD",
                "-noprompt");
        builder.environment().put("KMS_TEST_PASSWORD", password);
        var keytool = builder.redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
        if (!keytool.waitFor(30, TimeUnit.SECONDS)) {
            keytool.destroyForcibly();
            throw new IllegalStateException("Test keytool timed out");
        }
        assertThat(keytool.exitValue()).isZero();
        var store = KeyStore.getInstance("PKCS12");
        try (var input = Files.newInputStream(path)) {
            store.load(input, password.toCharArray());
        }
        return store;
    }

    private static ByteArrayInputStream serialized(KeyStore store, char[] password) throws Exception {
        var out = new ByteArrayOutputStream();
        store.store(out, password);
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Test
    void localBinderAutoConfigurationIsAbsentFromTheClasspath() {
        assertThat(ClassUtils.isPresent(
                        "io.github.loadup.components.kms.LocalKmsAutoConfiguration",
                        getClass().getClassLoader()))
                .isFalse();
    }

    private static byte[] testFile(char[] storePassword, char[] keyPassword) throws Exception {
        var generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        var store = KeyStore.getInstance("PKCS12");
        store.load(null, storePassword);
        store.setEntry(
                "payments",
                new KeyStore.SecretKeyEntry(generator.generateKey()),
                new KeyStore.PasswordProtection(keyPassword));
        var out = new ByteArrayOutputStream();
        store.store(out, storePassword);
        return out.toByteArray();
    }
}
