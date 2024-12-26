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
 * MD系类摘要算法, 支持算法：MD2，MD4，MD5
 */
@Component
public class AlgMD extends AbstractAlgorithm {
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
     *
     */
    @Override
    public byte[] digest(byte[] data, String algorithm) {

        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return md.digest(data);
        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign(algorithm) + "digest error:");

            throw new CertificationException(CertificationErrorCode.DIGEST_ERROR, genLogSign(algorithm), e);
        }
    }

    /**
     * 注册算法类到对应manager接口
     */
    @Override
    protected void doRegisterManager() {
        DigestManager.registerAlgo(AlgorithmEnum.MD2, this);
        DigestManager.registerAlgo(AlgorithmEnum.MD4, this);
        DigestManager.registerAlgo(AlgorithmEnum.MD5, this);
    }
}