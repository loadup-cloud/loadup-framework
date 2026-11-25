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
