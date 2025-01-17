package com.github.loadup.components.gateway.certification.manager;

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

import com.github.loadup.components.gateway.certification.algo.Algorithm;
import com.github.loadup.components.gateway.certification.exception.CertificationErrorCode;
import com.github.loadup.components.gateway.certification.exception.CertificationException;
import com.github.loadup.components.gateway.certification.impl.CertificationServiceImpl;
import com.github.loadup.components.gateway.certification.model.AlgorithmEnum;
import com.github.loadup.components.gateway.certification.model.CertificationFactor;
import com.github.loadup.components.gateway.facade.enums.CertTypeEnum;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 非对称加密算法管理类
 */
@Component
public class AsymmetricEncryptionManager extends AbstractAlgoManager {

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
     * 解密操作
     */
    @Override
    public String decrypt(String encryptedContent, CertificationFactor certificationFactor) {
        return (String) doOperation(certificationFactor, encryptedContent);
    }

    /**
     * 执行解密操作
     */
    @Override
    protected byte[] doDecrypt(byte[] srcInput, CertificationFactor certificationFactor) {
        Algorithm algorithm = getAlgorithm(certificationFactor);
        byte[] key = null;
        if (certificationFactor.getCertMap().containsKey(CertTypeEnum.PUBLIC_KEY.getCertType())) {

            key = (byte[]) certificationFactor.getCertMap().get(CertTypeEnum.PUBLIC_KEY.getCertType());
            return algorithm.decrypt(srcInput, key, certificationFactor.getAlgoString(), false);

        } else if (certificationFactor.getCertMap().containsKey(CertTypeEnum.PRIVATE_KEY.getCertType())) {

            key = (byte[]) certificationFactor.getCertMap().get(CertTypeEnum.PRIVATE_KEY.getCertType());
            return algorithm.decrypt(srcInput, key, certificationFactor.getAlgoString(), true);

        }

        throw new CertificationException(CertificationErrorCode.CONFIG_ERROR);
    }

    /**
     * 加密接口
     */
    @Override
    public String encrypt(String srcContent, CertificationFactor certificationFactor) {
        return (String) doOperation(certificationFactor, srcContent);
    }

    /**
     * 执行加密操作
     */
    @Override
    protected byte[] doEncrypt(byte[] srcInput, CertificationFactor certificationFactor) {

        Algorithm algorithm = getAlgorithm(certificationFactor);
        byte[] key = null;
        if (certificationFactor.getCertMap().containsKey(CertTypeEnum.PUBLIC_KEY.getCertType())) {

            key = (byte[]) certificationFactor.getCertMap().get(CertTypeEnum.PUBLIC_KEY.getCertType());
            return algorithm.encrypt(srcInput, key, certificationFactor.getAlgoString(), false);

        } else if (certificationFactor.getCertMap().containsKey(CertTypeEnum.PRIVATE_KEY.getCertType())) {

            key = (byte[]) certificationFactor.getCertMap().get(CertTypeEnum.PRIVATE_KEY.getCertType());
            return algorithm.encrypt(srcInput, key, certificationFactor.getAlgoString(), true);

        }

        throw new CertificationException(CertificationErrorCode.CONFIG_ERROR);
    }

    /**
     * 获取支持算法映射表
     */
    @Override
    protected Map<AlgorithmEnum, Algorithm> getAlgorithmMap() {
        return algoMap;
    }

    /**
     * 判断证书是否是私钥，通过名字判断来实现
     */
    private boolean isPrivateKey(String certTypeStr) {
        CertTypeEnum certType = CertTypeEnum.getEnumByType(certTypeStr);
        return certType == CertTypeEnum.PRIVATE_KEY;
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        CertificationServiceImpl.registerManager("ASYMMETRIC_ENCRYPTION", this);
    }
}
