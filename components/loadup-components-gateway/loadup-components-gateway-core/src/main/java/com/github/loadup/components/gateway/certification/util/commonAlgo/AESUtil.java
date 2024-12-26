package com.github.loadup.components.gateway.certification.util.commonAlgo;

import com.github.loadup.components.gateway.certification.util.Base64Util;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

/**
 * AES 对称加解密算法类
 * 说明：秘钥算法为 AES
 * 加密模式：ECB、CBC、CFB、OFB
 * 填充模式：NoPadding、PKCS1Padding、PKCS5Padding、PKCS7Padding 等
 * 输入算法名：形如 AES/ECB/NoPadding
 */
public class AESUtil {

    /**
     * DES算法名字
     */
    private static String KEY_ALGO_NAME = "AES";

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 通用加密处理
     */
    public static byte[] encrypt(byte[] data, byte[] key, String algorithm) throws Exception {

        Cipher cipher = Cipher.getInstance(algorithm);

        if (isCBCMode(algorithm)) {
            cipher.init(Cipher.ENCRYPT_MODE, recovertKey(key), getIv(cipher));
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, recovertKey(key));
        }
        return cipher.doFinal(data);
    }

    /**
     * 带盐加密处理
     *
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, byte[] key, String salt,
                                 String algorithm) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        IvParameterSpec iv = convertIv(cipher, salt);
        if (isCBCMode(algorithm)) { //只有cbc才能带盐
            cipher.init(Cipher.ENCRYPT_MODE, recovertKey(key), iv);
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, recovertKey(key));
        }
        return cipher.doFinal(data);
    }

    /**
     * 通用解密处理
     */
    public static byte[] decrypt(byte[] data, byte[] key, String algorithm) throws Exception {

        Cipher cipher = Cipher.getInstance(algorithm);

        if (isCBCMode(algorithm)) {
            cipher.init(Cipher.DECRYPT_MODE, recovertKey(key), getIv(cipher));
        } else {
            cipher.init(Cipher.DECRYPT_MODE, recovertKey(key));
        }

        return cipher.doFinal(data);
    }

    /**
     * 带盐的解密处理
     *
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, byte[] key, String salt,
                                 String algorithm) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        IvParameterSpec iv = convertIv(cipher, salt);

        if (isCBCMode(algorithm)) { //只有CBC才需要带盐
            cipher.init(Cipher.DECRYPT_MODE, recovertKey(key), iv);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, recovertKey(key));
        }

        return cipher.doFinal(data);
    }

    /**
     * 生成AES算法秘钥的二进制数组
     */
    public static byte[] generateKey(int keyLen) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGO_NAME);

        keyGenerator.init(keyLen);

        SecretKey secretKey = keyGenerator.generateKey();

        return secretKey.getEncoded();

    }

    /**
     * 基于秘钥 byte数组生成秘钥
     */
    public static SecretKey recovertKey(byte[] key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, KEY_ALGO_NAME);
        return secretKey;
    }

    /**
     * 初始化向量
     */
    public static IvParameterSpec getIv(Cipher cipher) {

        byte[] iv = new byte[cipher.getBlockSize()];
        for (int i = 0; i < iv.length; i++) {
            iv[i] = 0;
        }
        return new IvParameterSpec(iv);
    }

    /**
     * convert salt
     */
    private static IvParameterSpec convertIv(Cipher cipher, String salt) {
        if (!StringUtils.isEmpty(salt)) {
            return new IvParameterSpec(Base64Util.decode(salt.getBytes()));
        } else {
            return getIv(cipher);
        }
    }

    /**
     * 判断算是否CBC模式，此方法值针对DES, DESede等算法适用
     */
    public static boolean isCBCMode(String algoStr) {
        return StringUtils.contains(algoStr, "CBC");
    }
}