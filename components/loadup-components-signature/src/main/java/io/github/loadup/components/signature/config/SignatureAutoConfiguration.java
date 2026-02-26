package io.github.loadup.components.signature.config;

import io.github.loadup.components.signature.properties.SignatureProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * Signature 自动配置
 *
 * @author loadup
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.components.signature", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SignatureProperties.class)
@ComponentScan(basePackages = "io.github.loadup.components.signature")
public class SignatureAutoConfiguration {

    public SignatureAutoConfiguration(SignatureProperties properties) {
        log.info("LoadUp Signature 组件已启用: defaultSignatureAlgorithm={}, defaultDigestAlgorithm={}",
                properties.getDefaultSignatureAlgorithm(), properties.getDefaultDigestAlgorithm());
    }
}

