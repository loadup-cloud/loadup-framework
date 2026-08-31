package io.github.loadup.components.signature.service.impl;

/*-
 * #%L
 * LoadUp Components :: Signature
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
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
 * #L%
 */

import io.github.loadup.components.signature.enums.KeyAlgorithm;
import io.github.loadup.components.signature.exception.SignatureException;
import io.github.loadup.components.signature.model.KeyPairInfo;
import io.github.loadup.components.signature.properties.SignatureProperties;
import io.github.loadup.components.signature.service.KeyPairService;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 密钥对管理服务实现
 *
 * @author loadup
 */
@Service
public class KeyPairServiceImpl implements KeyPairService {
    private static final Logger log = LoggerFactory.getLogger(KeyPairServiceImpl.class);

    private final SignatureProperties properties;

    @Override
    public KeyPairInfo generateKeyPair(KeyAlgorithm algorithm, int keySize) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm.getJcaName());
            keyPairGenerator.initialize(keySize);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            String publicKeyBase64 =
                    Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKeyBase64 =
                    Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

            return KeyPairInfo.builder()
                    .publicKey(publicKeyBase64)
                    .privateKey(privateKeyBase64)
                    .algorithm(algorithm.getJcaName())
                    .keySize(keySize)
                    .build();
        } catch (Exception e) {
            log.error("密钥对生成失败: algorithm={}, keySize={}, error={}", algorithm, keySize, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.KEY_GENERATION_FAILED, "密钥对生成失败: " + e.getMessage(), e);
        }
    }

    @Override
    public KeyPairInfo generateKeyPair(KeyAlgorithm algorithm) {
        int keySize = properties.getKeySize(algorithm.name().toLowerCase(Locale.ROOT));
        return generateKeyPair(algorithm, keySize);
    }

    @Override
    public PrivateKey loadPrivateKey(String base64PrivateKey, KeyAlgorithm algorithm) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm.getJcaName());
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("私钥加载失败: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.INVALID_KEY, "私钥加载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public PublicKey loadPublicKey(String base64PublicKey, KeyAlgorithm algorithm) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm.getJcaName());
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            log.error("公钥加载失败: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.INVALID_KEY, "公钥加载失败: " + e.getMessage(), e);
        }
    }

    public KeyPairServiceImpl(SignatureProperties properties) {
        this.properties = properties;
    }

    public Logger getLog() {
        return this.log;
    }

    public SignatureProperties getProperties() {
        return this.properties;
    }
}
