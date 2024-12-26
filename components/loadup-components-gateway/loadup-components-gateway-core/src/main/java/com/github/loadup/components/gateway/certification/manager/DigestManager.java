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
 * 摘要算法管理类
 */
@Component
public class DigestManager extends AbstractAlgoManager {

    /**
     * 算法Map
     */
    private static Map<AlgorithmEnum, Algorithm> algoMap = new HashMap<AlgorithmEnum, Algorithm>();

    /**
     * 注册算法
     */
    public static void registerAlgo(AlgorithmEnum algo, Object obj) {
        algoMap.put(algo, (Algorithm) obj);
    }

    /**
     *
     */
    @Override
    public String digest(String srcContent, CertificationFactor certificationFactor) {
        return (String) doOperation(certificationFactor, srcContent);
    }

    /**
     * 执行获取摘要操作
     */
    @Override
    protected byte[] doDigest(byte[] srcInput, CertificationFactor certificationFactor) {
        Algorithm algorithm = getAlgorithm(certificationFactor);
        // todo xiaokai.sxk  2016/12/8-18:43  摘要支持加盐改造
        if (certificationFactor.getCertMap() == null || certificationFactor.getCertMap().isEmpty()) {
            return algorithm.digest(srcInput, certificationFactor.getAlgoString());
        } else {
            byte[] key = (byte[]) certificationFactor.getCertMap().get(
                    CertTypeEnum.DIGEST_KEY.getCertType());
            return algorithm.digest(srcInput, key, certificationFactor.getAlgoString());
        }

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
        CertificationServiceImpl.registerManager("DIGEST", this);
    }
}