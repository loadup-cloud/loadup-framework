package com.github.loadup.components.gateway.certification.algo;

import com.github.loadup.components.gateway.certification.exception.CertificationErrorCode;
import com.github.loadup.components.gateway.certification.exception.CertificationException;
import com.github.loadup.components.gateway.certification.manager.SymmetricEncryptionManager;
import com.github.loadup.components.gateway.certification.model.AlgorithmEnum;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

/**
 * IDEA对称加解密算法类
 */
@Component
public class AlgIDEA extends AbstractAlgorithm {
    /**
     * 日志定义
     */
    private static Logger logger = LoggerFactory.getLogger("CERT-ALGO");

    /**
     * DES算法名字
     */
    private static String KEY_ALGO_NAME = "IDEA";

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 通用加密处理
     */
    @Override
    public byte[] encrypt(byte[] data, byte[] key, String algorithm) {

        try {

            Cipher cipher = Cipher.getInstance(algorithm);

            cipher.init(Cipher.ENCRYPT_MODE, recovertKey(key));

            return cipher.doFinal(data);

        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign(algorithm) + "encrypt error:");
            throw new CertificationException(CertificationErrorCode.ENCRYPT_ERROR,
                    genLogSign(algorithm), e);
        }
    }

    /**
     * 通用解密处理
     */
    @Override
    public byte[] decrypt(byte[] data, byte[] key, String algorithm) {

        try {
            Cipher cipher = Cipher.getInstance(algorithm);

            cipher.init(Cipher.DECRYPT_MODE, recovertKey(key));

            return cipher.doFinal(data);

        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign(algorithm) + "encrypt error:");

            throw new CertificationException(CertificationErrorCode.DECRYPT_ERROR,
                    genLogSign(algorithm), e);
        }
    }

    /**
     * 生成AES算法秘钥的二进制数组
     */
    public static byte[] generateKey(int keyLen) {
        try {

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGO_NAME);

            keyGenerator.init(keyLen);

            SecretKey secretKey = keyGenerator.generateKey();

            return secretKey.getEncoded();

        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign(KEY_ALGO_NAME) + "generate key error:");
        }
        return null;
    }

    /**
     * 基于秘钥 byte数组生成秘钥
     */
    public static SecretKey recovertKey(byte[] key) {

        try {

            SecretKey secretKey = new SecretKeySpec(key, KEY_ALGO_NAME);
            return secretKey;

        } catch (Exception e) {
            LogUtil.error(logger, e, genLogSign(KEY_ALGO_NAME) + "recovery key error:");

            throw new CertificationException(CertificationErrorCode.RECOVER_KEY_ERROR,
                    genLogSign(KEY_ALGO_NAME), e);
        }
    }

    /**
     * 注册算法类到对应manager接口
     */
    @Override
    protected void doRegisterManager() {
        SymmetricEncryptionManager.registerAlgo(AlgorithmEnum.IDEA, this);
    }
}