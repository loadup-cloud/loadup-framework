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
import com.github.loadup.components.gateway.certification.impl.CertificationServiceImpl;
import com.github.loadup.components.gateway.certification.model.AlgorithmEnum;
import com.github.loadup.components.gateway.certification.model.CertificationFactor;
import com.github.loadup.components.gateway.certification.model.CommonParameter;
import com.github.loadup.components.gateway.facade.enums.CertTypeEnum;

import java.util.HashMap;
import java.util.Map;

public class Pkcs7SignatureManager extends AbstractAlgoManager {

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
        //AlgorithmEnum algoName = AlgorithmEnum.getByName(certificationFactor.getAlgoString());
        byte[] key = (byte[]) certificationFactor.getCertMap()
                .get(CertTypeEnum.PRIVATE_KEY.getCertType());
        byte[] certData = (byte[]) certificationFactor.getCertMap()
                .get(CertTypeEnum.PUBLIC_CERT.getCertType());
        String attach = (null == certificationFactor.getAlgoParameter()) ? null
                : certificationFactor.getAlgoParameter().get(CommonParameter.ATTACH);
        return algorithm.sign(srcInput, key, certData, "SHA1withRSA", Boolean.parseBoolean(attach));
    }

    /**
     * 验签接口, 对于xml验签操作，报文和签名结果在一起存放，都存放在signedContent中，srcContent == null
     */
    @Override
    public boolean verify(String srcContent, String signedContent,
                          CertificationFactor certificationFactor) {
        return (Boolean) doOperation(certificationFactor, srcContent, signedContent);
    }

    /**
     * 执行验签操作 对于xml验签操作，报文和签名结果在一起存放，都存放在unsignedData中，signedContent输入为null
     */
    @Override
    protected Boolean doVerify(byte[] unsignedData, byte[] signedData,
                               CertificationFactor certificationFactor) {
        Algorithm algorithm = getAlgorithm(certificationFactor);
        byte[] key = (byte[]) certificationFactor.getCertMap()
                .get(CertTypeEnum.PUBLIC_KEY.getCertType());
        String attach = (null == certificationFactor.getAlgoParameter()) ? null
                : certificationFactor.getAlgoParameter().get(CommonParameter.ATTACH);
        return algorithm.verify(unsignedData, signedData, key, "SHA1withRSA",
                Boolean.parseBoolean(attach));
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
     *                   as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        CertificationServiceImpl.registerManager(AlgorithmEnum.PKCS7_SHA1.getName(), this);
    }

}
