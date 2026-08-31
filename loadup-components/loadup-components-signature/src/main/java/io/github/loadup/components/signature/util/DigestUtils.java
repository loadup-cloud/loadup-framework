package io.github.loadup.components.signature.util;

/*-
 * #%L
 * LoadUp Components Signature
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static digest helpers backed by JCA {@code MessageDigest} / {@code Mac}.
 */
public final class DigestUtils {
    private static final Logger log = LoggerFactory.getLogger(DigestUtils.class);

    private DigestUtils() {}

    /**
     * Computes the MD5 digest of the given string.
     *
     * @param data the input string
     * @return the hex-encoded digest
     */
    public static String md5(String data) {
        return digest(data, DigestAlgorithm.MD5);
    }

    /**
     * Computes the SHA-256 digest of the given string.
     *
     * @param data the input string
     * @return the hex-encoded digest
     */
    public static String sha256(String data) {
        return digest(data, DigestAlgorithm.SHA256);
    }

    /**
     * Computes the SHA-512 digest of the given string.
     *
     * @param data the input string
     * @return the hex-encoded digest
     */
    public static String sha512(String data) {
        return digest(data, DigestAlgorithm.SHA512);
    }

    /**
     * Computes the HMAC-SHA256 of the given string with the given key.
     *
     * @param data the input string
     * @param key the secret key
     * @return the hex-encoded MAC
     */
    public static String hmacSha256(String data, String key) {
        return hmac(data, key, DigestAlgorithm.HMAC_SHA256);
    }

    /**
     * Computes the HMAC-SHA512 of the given string with the given key.
     *
     * @param data the input string
     * @param key the secret key
     * @return the hex-encoded MAC
     */
    public static String hmacSha512(String data, String key) {
        return hmac(data, key, DigestAlgorithm.HMAC_SHA512);
    }

    /**
     * Computes the digest of the given string using the given algorithm.
     *
     * @param data the input string
     * @param algorithm the non-HMAC digest algorithm
     * @return the hex-encoded digest
     */
    public static String digest(String data, DigestAlgorithm algorithm) {
        if (algorithm.isHmac()) {
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.INVALID_ALGORITHM,
                    "HMAC algorithms must use the hmac() method");
        }
        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance(algorithm.getJcaName());
            return bytesToHex(md.digest(dataBytes));
        } catch (Exception e) {
            log.error("Digest failed: algorithm={}", algorithm, e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.DIGEST_FAILED, "Digest failed: " + e.getMessage(), e);
        }
    }

    public static String hmac(String data, String key, DigestAlgorithm algorithm) {
        if (!algorithm.isHmac()) {
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.INVALID_ALGORITHM,
                    "Non-HMAC algorithms must use the digest() method");
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm.getJcaName());
            Mac mac = Mac.getInstance(algorithm.getJcaName());
            mac.init(keySpec);
            return bytesToHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("HMAC failed: algorithm={}", algorithm, e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.DIGEST_FAILED, "HMAC failed: " + e.getMessage(), e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
