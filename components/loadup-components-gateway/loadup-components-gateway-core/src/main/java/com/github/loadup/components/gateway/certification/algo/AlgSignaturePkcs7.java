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
import com.github.loadup.components.gateway.certification.manager.Pkcs7SignatureManager;
import com.github.loadup.components.gateway.certification.model.AlgorithmEnum;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collection;
import java.util.Iterator;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * PKCS7 signature
 */
@Component
public class AlgSignaturePkcs7 extends AbstractAlgorithm {

    /**
     * 日志定义
     */
    private static Logger logger = LoggerFactory.getLogger("CERT-ALGO");

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
     * 恢复私钥
     */
    public static PrivateKey recoverPrivateKey(byte[] data) {
        try {
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(data);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGO_NAME);
            return keyFactory.generatePrivate(pkcs8EncodedKeySpec);

        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign(KEY_ALGO_NAME) + " recover privateKey error:");

            throw new CertificationException(
                    CertificationErrorCode.RECOVER_PRIVATE_KEY_ERROR, genLogSign(KEY_ALGO_NAME), e);
        }
    }

    /**
     * 恢复证书
     */
    public static X509Certificate recoverCertificate(byte[] data) {
        try {
            InputStream certIn = new ByteArrayInputStream(data);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(certIn);
        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign(KEY_ALGO_NAME) + " recover certificate error:");
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
            LogUtil.error(logger, e, genLogSign(KEY_ALGO_NAME) + " recover publicKey error:");

            throw new CertificationException(
                    CertificationErrorCode.RECOVER_PUBLIC_KEY_ERROR, genLogSign(KEY_ALGO_NAME), e);
        }
    }

    /**
     * @see Algorithm#sign#sign(byte[], byte[], String)
     */
    @Override
    public byte[] sign(byte[] data, byte[] key, byte[] cert, String algorithm, boolean attach) {
        try {
            CMSTypedData cmsData = new CMSProcessableByteArray(data); // 消息明文对象
            CMSSignedDataGenerator gen = buildSignedGenerator(key, cert, algorithm);
            CMSSignedData signed = gen.generate(cmsData, attach);
            return signed.getEncoded();
        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign("PKCS7") + " sign error:");
            throw new CertificationException(CertificationErrorCode.SIGN_ERROR, genLogSign("PKCS7"), e);
        }
    }

    /**
     * @see Algorithm#verify(byte[], byte[], byte[], String, boolean)
     */
    @Override
    public boolean verify(byte[] unSignedData, byte[] signedData, byte[] key, String algorithm, boolean attach) {
        try {
            CMSSignedData cmsdata = null;
            if (attach) { // 签名包含原文
                cmsdata = new CMSSignedData(signedData);
            } else { // 签名里不包含原文
                CMSProcessableByteArray plain = new CMSProcessableByteArray(unSignedData);
                cmsdata = new CMSSignedData(plain, signedData);
            }
            SignerInformationVerifier verifier = this.buildSignerVerifier(key);
            Collection<SignerInformation> c = cmsdata.getSignerInfos().getSigners(); // 获取签名信息
            Iterator<SignerInformation> it = c.iterator();
            while (it.hasNext()) { // 验证每一个签名
                SignerInformation signer = it.next();
                if (!signer.verify(verifier)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign("PKCS7") + "verify error:");
            throw new CertificationException(CertificationErrorCode.SIGN_ERROR, genLogSign("PKCS7"), e);
        }
    }

    /**
     * @throws Exception
     */
    private CMSSignedDataGenerator buildSignedGenerator(byte[] key, byte[] cert, String algorithm) throws Exception {
        X509Certificate certificate = this.recoverCertificate(cert);
        ContentSigner signer =
                new JcaContentSignerBuilder(algorithm).setProvider("BC").build(recoverPrivateKey(key));
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        generator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder()
                        .setProvider("BC")
                        .build())
                .build(signer, certificate));
        // generator.addCertificates(null);
        return generator;
    }

    private SignerInformationVerifier buildSignerVerifier(byte[] key) throws Exception {
        JcaSimpleSignerInfoVerifierBuilder sigVerifBuilder = new JcaSimpleSignerInfoVerifierBuilder();
        SignerInformationVerifier signerInfoVerif =
                sigVerifBuilder.setProvider("BC").build(recoverPublicKey(key));
        return signerInfoVerif;
    }

    /**
     * 注册算法类到对应manager接口
     */
    @Override
    protected void doRegisterManager() {
        Pkcs7SignatureManager.registerAlgo(AlgorithmEnum.PKCS7_SHA1, this);
    }
}
