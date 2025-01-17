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
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.Security;

/**
 * SHA 系列摘要算法，支持算法, SHA-1,SHA-224,SHA-256,SHA-384,SHA-512
 */
@Component
public class AlgSHA extends AbstractAlgorithm {

    /**
     * 日志定义
     */
    private static Logger logger = LoggerFactory.getLogger("COMMON-CERT-ALGORITHM");

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 摘要算法
     */
    @Override
    public byte[] digest(byte[] data, String algorithm) {

        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return md.digest(data);
        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign(algorithm) + "message digest error:");

            throw new CertificationException(CertificationErrorCode.DIGEST_ERROR, genLogSign(algorithm), e);
        }

    }

    /**
     * 注册算法类到对应manager接口
     */
    @Override
    protected void doRegisterManager() {
        DigestManager.registerAlgo(AlgorithmEnum.SHA_1, this);
        DigestManager.registerAlgo(AlgorithmEnum.SHA_224, this);
        DigestManager.registerAlgo(AlgorithmEnum.SHA_256, this);
        DigestManager.registerAlgo(AlgorithmEnum.SHA_384, this);
        DigestManager.registerAlgo(AlgorithmEnum.SHA_512, this);
    }
}
