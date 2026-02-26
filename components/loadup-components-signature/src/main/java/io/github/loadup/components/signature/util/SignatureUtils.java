package io.github.loadup.components.signature.util;

import io.github.loadup.components.signature.enums.KeyAlgorithm;
import io.github.loadup.components.signature.enums.SignatureAlgorithm;
import io.github.loadup.components.signature.exception.SignatureException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 签名工具类（静态方法）
 *
 * @author loadup
 */
@Slf4j
public class SignatureUtils {

    private SignatureUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 快速 RSA 签名（使用 SHA256withRSA）
     *
     * @param data              待签名字符串
     * @param privateKeyBase64  Base64 编码的 RSA 私钥
     * @return Base64 编码的签名结果
     */
    public static String signRSA(String data, String privateKeyBase64) {
        return sign(data, privateKeyBase64, SignatureAlgorithm.SHA256_WITH_RSA);
    }

    /**
     * 快速 RSA 验签（使用 SHA256withRSA）
     *
     * @param data            原始字符串
     * @param signature       Base64 编码的签名
     * @param publicKeyBase64 Base64 编码的 RSA 公钥
     * @return true=验证通过, false=验证失败
     */
    public static boolean verifyRSA(String data, String signature, String publicKeyBase64) {
        return verify(data, signature, publicKeyBase64, SignatureAlgorithm.SHA256_WITH_RSA);
    }

    /**
     * 通用签名方法
     *
     * @param data              待签名字符串
     * @param privateKeyBase64  Base64 编码的私钥
     * @param algorithm         签名算法
     * @return Base64 编码的签名结果
     */
    public static String sign(String data, String privateKeyBase64, SignatureAlgorithm algorithm) {
        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            PrivateKey privateKey = loadPrivateKey(privateKeyBase64, algorithm.getKeyAlgorithm());

            Signature signature = Signature.getInstance(algorithm.getJcaName());
            signature.initSign(privateKey);
            signature.update(dataBytes);
            byte[] signatureBytes = signature.sign();

            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("签名失败: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.SIGN_FAILED,
                    "签名失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 通用验签方法
     *
     * @param data            原始字符串
     * @param signatureBase64 Base64 编码的签名
     * @param publicKeyBase64 Base64 编码的公钥
     * @param algorithm       签名算法
     * @return true=验证通过, false=验证失败
     */
    public static boolean verify(String data, String signatureBase64, String publicKeyBase64, SignatureAlgorithm algorithm) {
        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            PublicKey publicKey = loadPublicKey(publicKeyBase64, algorithm.getKeyAlgorithm());

            Signature signature = Signature.getInstance(algorithm.getJcaName());
            signature.initVerify(publicKey);
            signature.update(dataBytes);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            return signature.verify(signatureBytes);
        } catch (Exception e) {
            log.error("验签失败: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.VERIFY_FAILED,
                    "验签失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 加载私钥
     */
    private static PrivateKey loadPrivateKey(String base64PrivateKey, String keyAlgorithm) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 加载公钥
     */
    private static PublicKey loadPublicKey(String base64PublicKey, String keyAlgorithm) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
        return keyFactory.generatePublic(keySpec);
    }
}

