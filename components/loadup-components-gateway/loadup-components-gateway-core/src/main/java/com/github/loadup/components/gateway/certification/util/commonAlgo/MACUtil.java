package com.github.loadup.components.gateway.certification.util.commonAlgo;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

/**
 * MAC 系列算法(加入了秘钥的摘要算法),支持：HmacMD2,HmacMD4,HmacMD5,HmacSHA1,HmacSHA224,HmacSHA256,HmacSHA384,HmacSHA512
 */
public class MACUtil {

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * mac系列算法实现
     */
    public static byte[] digest(byte[] data, byte[] key, String algorithm) throws Exception {
        Mac mac = Mac.getInstance(algorithm);
        mac.init(recoverKey(key, algorithm));
        return mac.doFinal(data);
    }

    /**
     * 基于秘钥的二进制数组和算法还原秘钥
     */
    public static SecretKey recoverKey(byte[] data, String algorithm) {
        return new SecretKeySpec(data, algorithm);
    }

    /**
     * 生成算法对应的秘钥
     */
    public static byte[] generateKey(String algorithm) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();

    }

}