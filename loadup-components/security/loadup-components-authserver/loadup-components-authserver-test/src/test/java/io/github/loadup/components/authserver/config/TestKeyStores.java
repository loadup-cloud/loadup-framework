package io.github.loadup.components.authserver.config;

import io.github.loadup.components.authserver.sas.SasAuthServerAutoConfiguration;
import io.github.loadup.components.kms.LocalKmsAutoConfiguration;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/** Real file-backed keys created by the test JDK; no private-key fixtures are checked in. */
final class TestKeyStores {
    static final String PASSWORD = "synthetic-keystore-password";
    static final String PASSWORD_ENV = "LOADUP_TEST_KEYSTORE_PASSWORD";

    private TestKeyStores() {}

    static Path create(Path directory, String... aliases) throws Exception {
        Path store = directory.resolve("keys.p12");
        for (String alias : aliases) {
            var builder = new ProcessBuilder(
                    Path.of(System.getProperty("java.home"), "bin", "keytool").toString(),
                    "-genkeypair", "-alias", alias, "-keyalg", "RSA", "-keysize", "2048",
                    "-validity", "30", "-dname", "CN=AuthServer Test", "-storetype", "PKCS12",
                    "-keystore", store.toString(), "-storepass:env", PASSWORD_ENV,
                    "-keypass:env", PASSWORD_ENV, "-noprompt");
            builder.environment().put(PASSWORD_ENV, PASSWORD);
            var process = builder.redirectErrorStream(true).start();
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IllegalStateException("Test keytool timed out");
            }
            if (process.exitValue() != 0) {
                throw new IllegalStateException("Test keytool failed");
            }
        }
        return store;
    }

    static KeyStore load(Path path) throws Exception {
        var store = KeyStore.getInstance("PKCS12");
        try (var input = java.nio.file.Files.newInputStream(path)) {
            store.load(input, PASSWORD.toCharArray());
        }
        return store;
    }

    static ApplicationContextRunner runner(Path store) {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(LocalKmsAutoConfiguration.class, SasAuthServerAutoConfiguration.class))
                .withPropertyValues(
                        "loadup.kms.enabled=true",
                        "loadup.kms.binder=local",
                        "loadup.kms.local.key-store-location=" + store.toUri(),
                        "loadup.kms.local.key-store-password-env=" + PASSWORD_ENV,
                        PASSWORD_ENV + "=" + PASSWORD,
                        "loadup.security.auth-server.protocol-endpoints-enabled=false",
                        "loadup.security.auth-server.signing.active-key.name=auth-current",
                        "loadup.security.auth-server.audience=merchant-api");
    }
}
