package com.github.loadup.components.gateway.certification.manager;

import com.github.loadup.components.gateway.certification.algo.Algorithm;
import com.github.loadup.components.gateway.certification.impl.CertificationServiceImpl;
import com.github.loadup.components.gateway.certification.model.AlgorithmEnum;
import com.github.loadup.components.gateway.certification.model.CertificationFactor;
import com.github.loadup.components.gateway.facade.enums.CertTypeEnum;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 数字签名管理类
 */
@Component
public class DigitalSignatureManager extends AbstractAlgoManager {

    /**
     * 算法Map
     */
    private static Map<AlgorithmEnum, Algorithm> algoMap = new HashMap<AlgorithmEnum, Algorithm>();

    /**
     * 算法枚举类
     */
    public static void registerAlgo(AlgorithmEnum algo, Object obj) {
        algoMap.put(algo, (Algorithm) obj);
    }

    /**
     * 签名接口
     */
    @Override
    public String sign(String srcContent, CertificationFactor certificationFactor) {
        return (String) doOperation(certificationFactor, srcContent);
    }

    /**
     * 执行签名操作
     */
    @Override
    protected byte[] doSign(byte[] srcInput, CertificationFactor certificationFactor) {

        Algorithm algorithm = getAlgorithm(certificationFactor);
        byte[] key = (byte[]) certificationFactor.getCertMap().get(CertTypeEnum.PRIVATE_KEY.getCertType());
        return algorithm.sign(srcInput, key, certificationFactor.getAlgoString());

    }

    /**
     * 验签接口
     */
    @Override
    public boolean verify(String srcContent, String signedContent, CertificationFactor certificationFactor) {
        return (Boolean) doOperation(certificationFactor, srcContent, signedContent);
    }

    /**
     * 执行验签操作
     */
    @Override
    protected Boolean doVerify(byte[] unsignedData, byte[] signedData, CertificationFactor certificationFactor) {
        Algorithm algorithm = getAlgorithm(certificationFactor);
        byte[] key = (byte[]) certificationFactor.getCertMap().get(CertTypeEnum.PUBLIC_KEY.getCertType());
        return algorithm.verify(unsignedData, signedData, key, certificationFactor.getAlgoString());
    }

    /**
     * 获取支持算法映射表
     */
    @Override
    protected Map<AlgorithmEnum, Algorithm> getAlgorithmMap() {
        return algoMap;
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     * as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        CertificationServiceImpl.registerManager("DIGITAL_SIGNATURE", this);
    }
}