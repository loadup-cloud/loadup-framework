package io.github.loadup.components.signature.util;

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
import io.github.loadup.components.signature.exception.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;

/**
 * 摘要工具类（静态方法）
 *
 * @author loadup
 */
@Slf4j
public class DigestUtils {

    private DigestUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 快速 MD5
     *
     * @param data 待计算字符串
     * @return Hex 编码的 MD5 结果
     */
    public static String md5(String data) {
        return digest(data, DigestAlgorithm.MD5);
    }

    /**
     * 快速 SHA-256
     *
     * @param data 待计算字符串
     * @return Hex 编码的 SHA-256 结果
     */
    public static String sha256(String data) {
        return digest(data, DigestAlgorithm.SHA256);
    }

    /**
     * 快速 SHA-512
     *
     * @param data 待计算字符串
     * @return Hex 编码的 SHA-512 结果
     */
    public static String sha512(String data) {
        return digest(data, DigestAlgorithm.SHA512);
    }

    /**
     * 通用摘要方法
     *
     * @param data      待计算字符串
     * @param algorithm 摘要算法
     * @return Hex 编码的摘要结果
     */
    public static String digest(String data, DigestAlgorithm algorithm) {
        if (algorithm.isHmac()) {
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.INVALID_ALGORITHM, "HMAC 算法需要使用 hmac() 方法");
        }

        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getJcaName());
            byte[] hashBytes = messageDigest.digest(dataBytes);
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            log.error("摘要计算失败: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.DIGEST_FAILED, "摘要计算失败: " + e.getMessage(), e);
        }
    }

    /**
     * HMAC-SHA256
     *
     * @param data 待计算字符串
     * @param key  密钥
     * @return Hex 编码的 HMAC 结果
     */
    public static String hmacSha256(String data, String key) {
        return hmac(data, key, DigestAlgorithm.HMAC_SHA256);
    }

    /**
     * HMAC-SHA512
     *
     * @param data 待计算字符串
     * @param key  密钥
     * @return Hex 编码的 HMAC 结果
     */
    public static String hmacSha512(String data, String key) {
        return hmac(data, key, DigestAlgorithm.HMAC_SHA512);
    }

    /**
     * 通用 HMAC 方法
     *
     * @param data      待计算字符串
     * @param key       密钥
     * @param algorithm HMAC 算法
     * @return Hex 编码��� HMAC 结果
     */
    public static String hmac(String data, String key, DigestAlgorithm algorithm) {
        if (!algorithm.isHmac()) {
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.INVALID_ALGORITHM, "非 HMAC 算法需要使用 digest() 方法");
        }

        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, algorithm.getJcaName());
            Mac mac = Mac.getInstance(algorithm.getJcaName());
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(dataBytes);

            return bytesToHex(hashBytes);
        } catch (Exception e) {
            log.error("HMAC 计算失败: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.DIGEST_FAILED, "HMAC 计算失败: " + e.getMessage(), e);
        }
    }

    /**
     * 字节数组转 Hex 字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
