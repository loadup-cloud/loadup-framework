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

import io.github.loadup.components.signature.enums.DigestAlgorithm;
import io.github.loadup.components.signature.exception.SignatureException;
import io.github.loadup.components.signature.service.DigestService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCA-backed {@link DigestService} implementation.
 */
public class DigestServiceImpl implements DigestService {
    private static final Logger log = LoggerFactory.getLogger(DigestServiceImpl.class);

    @Override
    public String digest(byte[] data, DigestAlgorithm algorithm) {
        if (algorithm.isHmac()) {
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.INVALID_ALGORITHM,
                    "HMAC algorithms must use the hmac() method");
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getJcaName());
            byte[] hashBytes = messageDigest.digest(data);
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            log.error("Digest failed: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.DIGEST_FAILED, "Digest failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String digest(String data, DigestAlgorithm algorithm) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        return digest(dataBytes, algorithm);
    }

    @Override
    public String hmac(byte[] data, byte[] key, DigestAlgorithm algorithm) {
        if (!algorithm.isHmac()) {
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.INVALID_ALGORITHM,
                    "Non-HMAC algorithms must use the digest() method");
        }

        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, algorithm.getJcaName());
            Mac mac = Mac.getInstance(algorithm.getJcaName());
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(data);
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            log.error("HMAC failed: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.DIGEST_FAILED, "HMAC failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String hmac(String data, String key, DigestAlgorithm algorithm) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        return hmac(dataBytes, keyBytes, algorithm);
    }

    /** Converts a byte array into a lowercase hex string. */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
