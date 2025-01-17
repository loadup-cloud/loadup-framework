package com.github.loadup.components.gateway.certification.util.commonAlgo;

/*-
 * #%L
 * loadup-components-gateway-core
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Security;

/**
 * DES对称加密算法类
 * 说明：秘钥算法为 DES
 * 加密模式：ECB、CBC、CFB、OFB
 * 填充模式：NoPadding、PKCS1Padding、PKCS5Padding、PKCS7Padding 等
 * 输入算法名：形如 DES/ECB/NoPadding
 */
public class DESUtil {

    /**
     * DES算法名字
     */
    private static String KEY_ALGO_NAME = "DES";

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
     * 生成DES算法秘钥的二进制数组
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

        DESKeySpec desKeySpec = new DESKeySpec(key);

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGO_NAME);

        return keyFactory.generateSecret(desKeySpec);
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
     * 判断算是否CBC模式，此方法值针对DES, DESede等算法适用
     */
    public static boolean isCBCMode(String algoStr) {
        return StringUtils.contains(algoStr, "CBC");
    }
}
