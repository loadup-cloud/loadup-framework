package io.github.loadup.components.signature.config;

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
