package com.github.loadup.components.gateway.certification.util.commonAlgo;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.Security;

/**
 * 国密算法
 */
public class SMecbUtil {

    /**
     * DES算法名字
     */
    private static String KEY_ALGO_NAME = "SM4";

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 通用加密处理
     */
    public static byte[] encrypt(byte[] data, byte[] key, String algorithm) throws Exception {
        Key kkey = new SecretKeySpec(key, KEY_ALGO_NAME);
        Cipher bcCipher = Cipher.getInstance(algorithm, "BC");
        bcCipher.init(Cipher.ENCRYPT_MODE, kkey);
        return bcCipher.doFinal(data);
    }

    /**
     * 通用解密处理
     */
    public static byte[] decrypt(byte[] data, byte[] key, String algorithm) throws Exception {
        Key kkey = new SecretKeySpec(key, KEY_ALGO_NAME);
        Cipher bcCipher = Cipher.getInstance(algorithm, "BC");
        bcCipher.init(Cipher.DECRYPT_MODE, kkey);
        return bcCipher.doFinal(data);
    }

    /**
     * 生成SM4算法秘钥的二进制数组
     */
    public static byte[] generateKey(int keyLen) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGO_NAME, "BC");
        keyGenerator.init(keyLen);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }

}
