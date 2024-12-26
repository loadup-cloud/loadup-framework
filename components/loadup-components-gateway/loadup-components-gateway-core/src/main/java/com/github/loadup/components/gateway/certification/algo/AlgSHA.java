package com.github.loadup.components.gateway.certification.algo;

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