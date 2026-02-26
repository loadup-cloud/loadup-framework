package io.github.loadup.components.signature.properties;

import io.github.loadup.components.signature.enums.DigestAlgorithm;
import io.github.loadup.components.signature.enums.SignatureAlgorithm;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

/**
 * Signature 配置属性
 *
 * @author loadup
 */
@Data
@Validated
@ConfigurationProperties(prefix = "loadup.components.signature")
public class SignatureProperties {

    /**
     * 是否启用签名组件
     */
    private boolean enabled = true;

    /**
     * 默认签名算法
     */
    @NotNull
    private SignatureAlgorithm defaultSignatureAlgorithm = SignatureAlgorithm.SHA256_WITH_RSA;

    /**
     * 默认摘要算法
     */
    @NotNull
    private DigestAlgorithm defaultDigestAlgorithm = DigestAlgorithm.SHA256;

    /**
     * 密钥长度配置
     */
    private Map<String, Integer> keySize = new HashMap<>() {{
        put("rsa", 2048);
        put("dsa", 2048);
        put("ec", 256);
    }};

    /**
     * 获取指定算法的密钥长度
     *
     * @param algorithm 算法名称 (rsa/dsa/ec)
     * @return 密钥长度
     */
    public int getKeySize(String algorithm) {
        return keySize.getOrDefault(algorithm.toLowerCase(), 2048);
    }
}

