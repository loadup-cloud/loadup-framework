package io.github.loadup.components.signature.util;

/*-
 * #%L
 * LoadUp Components Signature
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
import io.github.loadup.components.signature.exception.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Digest utility wrapping commons with signature-specific enum support.
 */
public final class DigestUtils {
    private static final Logger log = LoggerFactory.getLogger(DigestUtils.class);

    private DigestUtils() {}

    public static String md5(String data) {
        return DigestUtils.md5(data);
    }

    public static String sha256(String data) {
        return DigestUtils.sha256(data);
    }

    public static String sha512(String data) {
        return DigestUtils.sha512(data);
    }

    public static String hmacSha256(String data, String key) {
        return DigestUtils.hmacSha256(data, key);
    }

    public static String hmacSha512(String data, String key) {
        return DigestUtils.hmacSha512(data, key);
    }

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

    public Logger getLog() {
        return this.log;
    }
}
