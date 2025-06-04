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
import com.github.loadup.components.gateway.certification.manager.DigestManager;
import com.github.loadup.components.gateway.certification.model.AlgorithmEnum;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import java.security.Security;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * MAC 系列算法(加入了秘钥的摘要算法),支持：HmacMD2,HmacMD4,HmacMD5,HmacSHA1,HmacSHA224,HmacSHA256,HmacSHA384,HmacSHA512
 */
@Component
public class AlgMAC extends AbstractAlgorithm {

    /**
     * 日志定义
     */
    private static Logger logger = LoggerFactory.getLogger("CERT-ALGO");

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
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
    public static byte[] generateKey(String algorithm) {
        try {

            KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
            SecretKey secretKey = keyGenerator.generateKey();
            return secretKey.getEncoded();

        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign(algorithm) + "generate key error:");
        }
        return null;
    }

    /**
     * mac系列算法实现
     */
    @Override
    public byte[] digest(byte[] data, byte[] key, String algorithm) {
        try {

            Mac mac = Mac.getInstance(algorithm);
            mac.init(recoverKey(key, algorithm));
            return mac.doFinal(data);

        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign(algorithm) + "execute error:");
            throw new CertificationException(CertificationErrorCode.DIGEST_ERROR, genLogSign(algorithm), e);
        }
    }

    /**
     * 注册算法类到对应manager接口
     */
    @Override
    protected void doRegisterManager() {
        DigestManager.registerAlgo(AlgorithmEnum.HmacMD2, this);
        DigestManager.registerAlgo(AlgorithmEnum.HmacMD4, this);
        DigestManager.registerAlgo(AlgorithmEnum.HmacMD5, this);
        DigestManager.registerAlgo(AlgorithmEnum.HmacSHA1, this);
        DigestManager.registerAlgo(AlgorithmEnum.HmacSHA224, this);
        DigestManager.registerAlgo(AlgorithmEnum.HmacSHA256, this);
        DigestManager.registerAlgo(AlgorithmEnum.HmacSHA256, this);
        DigestManager.registerAlgo(AlgorithmEnum.HmacSHA384, this);
        DigestManager.registerAlgo(AlgorithmEnum.HmacSHA512, this);
    }
}
