package com.github.loadup.components.gateway.certification.util.commonAlgo;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA非对称算法实现
 */
public class RSAUtil {

    /**
     * RSA 算法名字
     */
    private static String KEY_ALGO_NAME = "RSA";

    /**
     * 公钥名
     */
    private static String PUBLICK_KEY = "PUBLIC_KEY";

    /**
     * 私钥名
     */
    private static String PRIVATE_KEY = "PRIVATE_KEY";

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 公共加密接口
     */
    public static byte[] encrypt(byte[] data, byte[] key, String algorithm, boolean isPrivateKey)
            throws Exception {

        Key secretKey = isPrivateKey ? recoverPrivateKey(key) : recoverPublicKey(key);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    /**
     * 公共解密接口
     */
    public static byte[] decrypt(byte[] data, byte[] key, String algorithm, boolean isPrivateKey)
            throws Exception {
        Key secretKey = isPrivateKey ? recoverPrivateKey(key) : recoverPublicKey(key);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);

    }

    /**
     * 生成公私钥对
     */
    public static Map<String, Object> generateKey(int keySize) throws Exception {

        Map<String, Object> rtn = new HashMap<String, Object>();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGO_NAME);
        keyPairGenerator.initialize(keySize);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] privateKey = keyPair.getPrivate().getEncoded();
        rtn.put(PUBLICK_KEY, publicKey);
        rtn.put(PRIVATE_KEY, privateKey);

        return rtn;
    }

    /**
     * 恢复私钥
     */
    public static PrivateKey recoverPrivateKey(byte[] data) throws Exception {

        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(data);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGO_NAME);
        return keyFactory.generatePrivate(pkcs8EncodedKeySpec);

    }

    /**
     * 恢复公钥
     */
    public static PublicKey recoverPublicKey(byte[] data) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(data);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGO_NAME);
        return keyFactory.generatePublic(keySpec);
    }

}