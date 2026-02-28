package io.github.loadup.components.signature.service.impl;

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
import io.github.loadup.components.signature.service.DigestService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 摘要服务实现
 *
 * @author loadup
 */
@Slf4j
@Service
public class DigestServiceImpl implements DigestService {

    @Override
    public String digest(byte[] data, DigestAlgorithm algorithm) {
        if (algorithm.isHmac()) {
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.INVALID_ALGORITHM, "HMAC 算法需要使用 hmac() 方法");
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getJcaName());
            byte[] hashBytes = messageDigest.digest(data);
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            log.error("摘要计算失败: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.DIGEST_FAILED, "摘要计算失败: " + e.getMessage(), e);
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
                    SignatureException.SignatureErrorCode.INVALID_ALGORITHM, "非 HMAC 算法需要使用 digest() 方法");
        }

        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, algorithm.getJcaName());
            Mac mac = Mac.getInstance(algorithm.getJcaName());
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(data);
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            log.error("HMAC 计算失���: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.DIGEST_FAILED, "HMAC 计算失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String hmac(String data, String key, DigestAlgorithm algorithm) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        return hmac(dataBytes, keyBytes, algorithm);
    }

    /**
     * 字节数组转 Hex 字符串
     */
    private String bytesToHex(byte[] bytes) {
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
