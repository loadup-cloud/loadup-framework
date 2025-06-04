/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.certification.algo;

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

import com.github.loadup.components.gateway.certification.exception.CertificationErrorCode;
import com.github.loadup.components.gateway.certification.exception.CertificationException;
import com.github.loadup.components.gateway.certification.manager.DigitalSignatureManager;
import com.github.loadup.components.gateway.certification.model.AlgorithmEnum;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * DSA数字签名算法
 */
@Component
public class AlgDSA extends AbstractAlgorithm {

    /**
     * 日志定义
     */
    private static Logger logger = LoggerFactory.getLogger("CERT-ALGO");

    /**
     * RSA 算法名字
     */
    private static String KEY_ALGO_NAME = "DSA";

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
     * 生成公私钥对
     */
    public static Map<String, Object> generateKey(int keySize) {

        Map<String, Object> rtn = new HashMap<String, Object>();

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGO_NAME);
            keyPairGenerator.initialize(keySize, new SecureRandom());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            byte[] publicKey = keyPair.getPublic().getEncoded();
            byte[] privateKey = keyPair.getPrivate().getEncoded();
            rtn.put(PUBLICK_KEY, publicKey);
            rtn.put(PRIVATE_KEY, privateKey);
        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign(KEY_ALGO_NAME) + "generate key error:");
        }

        return rtn;
    }

    /**
     * 恢复私钥
     */
    public static PrivateKey recoverPrivateKey(byte[] data) {

        try {

            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(data);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGO_NAME);
            return keyFactory.generatePrivate(pkcs8EncodedKeySpec);

        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign(KEY_ALGO_NAME) + "recover privateKey error:");

            throw new CertificationException(CertificationErrorCode.RECOVER_KEY_ERROR, genLogSign(KEY_ALGO_NAME), e);
        }
    }

    /**
     * 恢复公钥
     */
    public static PublicKey recoverPublicKey(byte[] data) {
        try {

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(data);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGO_NAME);
            return keyFactory.generatePublic(keySpec);

        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign(KEY_ALGO_NAME) + "recover publicKey error:");

            throw new CertificationException(CertificationErrorCode.RECOVER_KEY_ERROR, genLogSign(KEY_ALGO_NAME), e);
        }
    }

    /**
     * 公共签名接口
     */
    @Override
    public byte[] sign(byte[] data, byte[] key, String algorithm) {
        byte[] rtn = null;
        try {

            PrivateKey privateKey = recoverPrivateKey(key);
            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(privateKey);
            signature.update(data);
            rtn = signature.sign();
            return rtn;

        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign(algorithm) + "sign error:");
            throw new CertificationException(CertificationErrorCode.SIGN_ERROR, genLogSign(algorithm), e);
        }
    }

    /**
     * 公共验签接口
     */
    @Override
    public boolean verify(byte[] unSignedData, byte[] signedData, byte[] key, String algorithm) {
        try {

            PublicKey publicKey = recoverPublicKey(key);
            Signature signature = Signature.getInstance(algorithm);
            signature.initVerify(publicKey);

            signature.update(unSignedData);
            return signature.verify(signedData);

        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign(algorithm) + "decrypt error:");

            throw new CertificationException(CertificationErrorCode.VERIFY_ERROR, genLogSign(algorithm), e);
        }
    }

    /**
     * 注册算法类到对应manager接口
     */
    @Override
    protected void doRegisterManager() {
        DigitalSignatureManager.registerAlgo(AlgorithmEnum.DSA, this);
    }
}
