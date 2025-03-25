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
import com.github.loadup.components.gateway.certification.model.AlgorithmEnum;
import com.github.loadup.components.gateway.certification.model.CertificationFactor;
import com.github.loadup.components.gateway.certification.model.CommonParameter;
import com.github.loadup.components.gateway.certification.util.Convertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * 抽象算法管理类
 */
public class AbstractAlgoManager implements AlgoManager, InitializingBean {

    /**
     * 日志定义
     */
    private static Logger logger = LoggerFactory.getLogger("ALGO-MANAGER");

    /**
     * 解密操作
     */
    @Override
    public String decrypt(String encryptedContent, CertificationFactor certificationFactor) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION);
    }

    /**
     * 获取摘要接口
     */
    @Override
    public String digest(String srcContent, CertificationFactor certificationFactor) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION);
    }

    /**
     * 加密接口
     */
    @Override
    public String encrypt(String srcContent, CertificationFactor certificationFactor) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION);
    }

    /**
     * 签名接口
     */
    @Override
    public String sign(String srcContent, CertificationFactor certificationFactor) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION);
    }

    /**
     * 验签接口
     */
    @Override
    public boolean verify(String srcContent, String signedContent,
                          CertificationFactor certificationFactor) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION);

    }

    /**
     * 特殊签名接口
     */
    @Override
    public String encode(String srcContent, CertificationFactor certificationFactor) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION);
    }

    /**
     * 特殊验签接口
     */
    @Override
    public String decode(String srcContent, String signedContent,
                         CertificationFactor certificationFactor) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION);

    }

    /**
     * 执行操作
     */
    protected Object doOperation(Object... objects) {

        CertificationFactor certificationFactor = (CertificationFactor) objects[0];
        String arg1 = null;
        byte[] output = null;

        switch (certificationFactor.getOperationType()) {
            case OP_SIGN:
                arg1 = (String) objects[1];
                output = doSign(Convertor.converInput(arg1, certificationFactor),
                        certificationFactor);
                return Convertor.convertOutput(output, certificationFactor);

            case OP_ENCODE:
                arg1 = (String) objects[1];
                output = doEncode(Convertor.converInput(arg1, certificationFactor),
                        certificationFactor);
                return Convertor.convertOutput(output, certificationFactor);

            case OP_ENCRYPT:
                arg1 = (String) objects[1];
                output = doEncrypt(Convertor.converInput(arg1, certificationFactor),
                        certificationFactor);
                return Convertor.convertOutput(output, certificationFactor);

            case OP_DECRYPT:
                arg1 = (String) objects[1];
                output = doDecrypt(Convertor.converInput(arg1, certificationFactor),
                        certificationFactor);
                return Convertor.convertOutput(output, certificationFactor);

            case OP_DIGEST:
                arg1 = (String) objects[1];
                output = doDigest(Convertor.converInput(arg1, certificationFactor),
                        certificationFactor);
                return Convertor.convertOutput(output, certificationFactor);

            case OP_VERIFY:
                String unsignedData = null;
                if (objects[1] != null) {
                    unsignedData = (String) objects[1];
                }
                String signedData = (String) objects[2];
                Map<String, byte[]> inputs = Convertor.converInput(unsignedData, signedData,
                        certificationFactor);
                return doVerify(inputs.get(CommonParameter.UNSIGNED_DATA),
                        inputs.get(CommonParameter.SIGNED_DATA), certificationFactor);

            case OP_DECODE:
                String unsignedData1 = null;
                if (objects[1] != null) {
                    unsignedData1 = (String) objects[1];
                }
                String signedData1 = (String) objects[2];
                Map<String, byte[]> inputs1 = Convertor.converInput(unsignedData1, signedData1,
                        certificationFactor);
                return doDecode(inputs1.get(CommonParameter.UNSIGNED_DATA),
                        inputs1.get(CommonParameter.SIGNED_DATA), certificationFactor);

            default:
                break;
        }
        return null;
    }

    /**
     * 执行签名操作
     */
    protected byte[] doSign(byte[] srcInput, CertificationFactor certificationFactor) {
        return null;
    }

    /**
     * 执行验签操作
     */
    protected Boolean doVerify(byte[] unsignedData, byte[] signedData,
                               CertificationFactor certificationFactor) {
        return false;
    }

    /**
     * 执行特殊签名操作
     */
    protected byte[] doEncode(byte[] srcInput, CertificationFactor certificationFactor) {
        return null;
    }

    /**
     * 执行特殊验签操作
     */
    protected String doDecode(byte[] unsignedData, byte[] signedData,
                              CertificationFactor certificationFactor) {
        return null;
    }

    /**
     * 执行加密操作
     */
    protected byte[] doEncrypt(byte[] srcInput, CertificationFactor certificationFactor) {
        return null;
    }

    /**
     * 执行解密操作
     */
    protected byte[] doDecrypt(byte[] srcInput, CertificationFactor certificationFactor) {
        return null;
    }

    /**
     * 执行获取摘要操作
     */
    protected byte[] doDigest(byte[] srcInput, CertificationFactor certificationFactor) {
        return null;
    }

    /**
     * 获取对应的算法类
     */
    public Algorithm getAlgorithm(CertificationFactor certificationFactor) {
        AlgorithmEnum algorithm = AlgorithmEnum.getByName(certificationFactor.getAlgoString());
        return getAlgorithmMap().get(algorithm);
    }

    /**
     * 获取支持算法映射表
     */
    protected Map<AlgorithmEnum, Algorithm> getAlgorithmMap() {
        return null;
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

    }

}
