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

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * 数字签名算法实现,算法可做为入参输入，当前支持算法：
 * NONEwithRSA
 * MD2withRSA
 * MD5withRSA
 * SHA1withRSA
 * SHA224withRSA
 * SHA256withRSA
 * SHA384withRSA
 * SHA512withRSA
 * RIPEMD128withRSA
 * RIPEMD160withRSA
 * SHA1withDSA
 * SHA224withDSA
 * SHA256withDSA
 * SHA384withDSA
 * SHA512withDSA
 */
public class SignatureRSAUtil {

    /**
     * 公钥名
     */
    public static String PUBLICK_KEY = "PUBLIC_KEY";
    /**
     * 私钥名
     */
    public static String PRIVATE_KEY = "PRIVATE_KEY";
    /**
     * RSA 算法名字
     */
    private static String KEY_ALGO_NAME = "RSA";

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 公共签名接口
     */
    public static byte[] sign(byte[] data, byte[] key, String algorithm) throws Exception {

        PrivateKey privateKey = recoverPrivateKey(key);
        Signature signature = Signature.getInstance(algorithm);
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();

    }

    /**
     * 公共验签接口
     */
    public static boolean verify(byte[] unSignedData, byte[] signedData, byte[] key,
                                String algorithm) throws Exception {
        PublicKey publicKey = recoverPublicKey(key);
        Signature signature = Signature.getInstance(algorithm);
        signature.initVerify(publicKey);

        signature.update(unSignedData);
        return signature.verify(signedData);

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
