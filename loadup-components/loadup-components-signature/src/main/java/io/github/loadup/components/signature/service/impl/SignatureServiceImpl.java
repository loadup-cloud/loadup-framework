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
import io.github.loadup.components.signature.enums.SignatureAlgorithm;
import io.github.loadup.components.signature.exception.SignatureException;
import io.github.loadup.components.signature.service.KeyPairService;
import io.github.loadup.components.signature.service.SignatureService;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCA-backed {@link SignatureService} implementation.
 */
public class SignatureServiceImpl implements SignatureService {
    private static final Logger log = LoggerFactory.getLogger(SignatureServiceImpl.class);

    private final KeyPairService keyPairService;

    @Override
    public String sign(byte[] data, PrivateKey privateKey, SignatureAlgorithm algorithm) {
        try {
            Signature signature = Signature.getInstance(algorithm.getJcaName());
            signature.initSign(privateKey);
            signature.update(data);
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("Sign failed: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.SIGN_FAILED, "Sign failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String sign(String data, String privateKeyBase64, SignatureAlgorithm algorithm) {
        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            PrivateKey privateKey =
                    keyPairService.loadPrivateKey(privateKeyBase64, KeyAlgorithm.valueOf(algorithm.getKeyAlgorithm()));
            return sign(dataBytes, privateKey, algorithm);
        } catch (SignatureException e) {
            throw e;
        } catch (Exception e) {
            log.error("Sign failed: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.SIGN_FAILED, "Sign failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verify(byte[] data, String signatureBase64, PublicKey publicKey, SignatureAlgorithm algorithm) {
        try {
            Signature signature = Signature.getInstance(algorithm.getJcaName());
            signature.initVerify(publicKey);
            signature.update(data);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            log.error("Verify failed: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.VERIFY_FAILED, "Verify failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verify(String data, String signatureBase64, String publicKeyBase64, SignatureAlgorithm algorithm) {
        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            PublicKey publicKey =
                    keyPairService.loadPublicKey(publicKeyBase64, KeyAlgorithm.valueOf(algorithm.getKeyAlgorithm()));
            return verify(dataBytes, signatureBase64, publicKey, algorithm);
        } catch (SignatureException e) {
            throw e;
        } catch (Exception e) {
            log.error("Verify failed: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.VERIFY_FAILED, "Verify failed: " + e.getMessage(), e);
        }
    }

    public SignatureServiceImpl(KeyPairService keyPairService) {
        this.keyPairService = keyPairService;
    }
}
