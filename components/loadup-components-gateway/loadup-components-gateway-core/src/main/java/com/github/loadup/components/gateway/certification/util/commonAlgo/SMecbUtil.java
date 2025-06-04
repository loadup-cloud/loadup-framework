/* Copyright (C) LoadUp Cloud 2022-2025 */
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

import java.security.Key;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

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
