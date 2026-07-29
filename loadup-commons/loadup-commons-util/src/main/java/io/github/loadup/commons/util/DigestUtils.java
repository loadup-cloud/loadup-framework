package io.github.loadup.commons.util;

/*-
 * #%L
 * Loadup Common Utils
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Digest & hash utility.
 *
 * <p>Provides MD5, SHA-256, SHA-512, HMAC-SHA256, and HMAC-SHA512 operations.
 */
public final class DigestUtils {

    private DigestUtils() {}

    public static String md5(String data) {
        return digest(data, "MD5");
    }

    public static String sha256(String data) {
        return digest(data, "SHA-256");
    }

    public static String sha512(String data) {
        return digest(data, "SHA-512");
    }

    public static String hmacSha256(String data, String key) {
        return hmac(data, key, "HmacSHA256");
    }

    public static String hmacSha512(String data, String key) {
        return hmac(data, key, "HmacSHA512");
    }

    private static String digest(String data, String algorithm) {
        try {
            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return bytesToHex(md.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Digest algorithm not available: " + algorithm, e);
        }
    }

    private static String hmac(String data, String key, String algorithm) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(keySpec);
            return bytesToHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("HMAC failed: " + algorithm, e);
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
