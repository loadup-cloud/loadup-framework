package io.github.loadup.components.signature.properties;

/*-
 * #%L
 * LoadUp Components :: Signature
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.components.signature.enums.DigestAlgorithm;
import io.github.loadup.components.signature.enums.SignatureAlgorithm;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

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
    private Map<String, Integer> keySize = new HashMap<>() {
        {
            put("rsa", 2048);
            put("dsa", 2048);
            put("ec", 256);
        }
    };

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
