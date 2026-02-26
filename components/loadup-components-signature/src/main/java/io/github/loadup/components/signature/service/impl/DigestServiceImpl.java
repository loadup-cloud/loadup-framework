package io.github.loadup.components.signature.service.impl;

import io.github.loadup.components.signature.enums.DigestAlgorithm;
import io.github.loadup.components.signature.exception.SignatureException;
import io.github.loadup.components.signature.service.DigestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

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
                    SignatureException.SignatureErrorCode.INVALID_ALGORITHM,
                    "HMAC 算法需要使用 hmac() 方法"
            );
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getJcaName());
            byte[] hashBytes = messageDigest.digest(data);
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            log.error("摘要计算失败: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.DIGEST_FAILED,
                    "摘要计算失败: " + e.getMessage(),
                    e
            );
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
                    "非 HMAC 算法需要使用 digest() 方法"
            );
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
                    SignatureException.SignatureErrorCode.DIGEST_FAILED,
                    "HMAC 计算失败: " + e.getMessage(),
                    e
            );
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

