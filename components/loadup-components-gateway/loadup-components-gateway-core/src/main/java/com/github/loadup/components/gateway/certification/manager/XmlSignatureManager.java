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
import com.github.loadup.components.gateway.certification.model.*;
import com.github.loadup.components.gateway.certification.util.CommonUtil;
import com.github.loadup.components.gateway.facade.enums.CertTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.signature.XMLSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * xml报文签名manager
 */
@Component
public class XmlSignatureManager extends AbstractAlgoManager {

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
		byte[] key = (byte[]) certificationFactor.getCertMap().get(
				CertTypeEnum.PRIVATE_KEY.getCertType());
		int appentMode = getAppendMode(certificationFactor);
		String eleTagName = getElementTagName(certificationFactor);
		String algoString = getAlgoStr(certificationFactor.getAlgoString());
		String encode = (null == certificationFactor.getAlgoParameter()) ? null
				: certificationFactor.getAlgoParameter().get(CommonParameter.INPUT_ENCODE);
		byte[] certData = null;
		if (needAppendIssuerSerial(certificationFactor)) {
			certData = (byte[]) certificationFactor.getCertMap().get(
					CertTypeEnum.PUBLIC_CERT.getCertType());
		}
		return algorithm.signXmlElement(key, certData, srcInput, encode, eleTagName, algoString,
				appentMode);
	}

	private boolean needAppendIssuerSerial(CertificationFactor certificationFactor) {
		if (null == certificationFactor.getAlgoParameter()
				|| StringUtils.isEmpty(certificationFactor.getAlgoParameter().get(
				CommonParameter.NEED_ISSUER_SERIAL))) {
			return false;
		}
		return Boolean.parseBoolean(certificationFactor.getAlgoParameter().get(
				CommonParameter.NEED_ISSUER_SERIAL));
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
		byte[] key = (byte[]) certificationFactor.getCertMap().get(
				CertTypeEnum.PUBLIC_KEY.getCertType());
		String encode = (null == certificationFactor.getAlgoParameter()) ? null
				: certificationFactor.getAlgoParameter().get(CommonParameter.UNSIGNED_DATA_ENCODE);
		return algorithm.verifyXmlElement(key, signedData, encode);
	}

	/**
	 * 获取支持算法映射表
	 */
	@Override
	protected Map<AlgorithmEnum, Algorithm> getAlgorithmMap() {
		return algoMap;
	}

	/**
	 * xml签名算法根据算法名Id获取算法名，使用反射获取
	 */
	private String getAlgoStr(String algoName) {
		try {
			Field field = XMLSignature.class.getField(algoName);
			field.setAccessible(true);
			return (String) field.get(XMLSignature.class);
		} catch (Exception e) {
			throw new CertificationException(CertificationErrorCode.UNSUPPORTED_ALGORITHM, CommonUtil
					.decorateBySquareBrackets("xml algo"), e);
		}
	}

	/**
	 * 获取xml签名算法的append Mode， 没有设置默认为作为子节点
	 */
	private int getAppendMode(CertificationFactor certificationFactor) {

		String appendMode = certificationFactor.getAlgoParameter().get(CommonParameter.APPEND_MODE);
		if (appendMode == null) {
			return XmlSignatureAppendMode.AS_CHILDREN;
		}
		return Integer.valueOf(appendMode).intValue();
	}

	/**
	 * 获取签名部分内容的tag名
	 */
	private String getElementTagName(CertificationFactor certificationFactor) {
		String tagName = certificationFactor.getAlgoParameter().get(
				CommonParameter.XML_ELE_TAG_NAME);
		if (tagName == null) {
			return CommonParameter.XML_ELE_TAG_DEFAULT;
		}

		return tagName;
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
		CertificationServiceImpl.registerManager("XML_SIGNATURE", this);
	}
}
