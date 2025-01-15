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
import com.github.loadup.components.gateway.certification.manager.XmlSignatureManager;
import com.github.loadup.components.gateway.certification.model.AlgorithmEnum;
import com.github.loadup.components.gateway.certification.model.XmlSignatureAppendMode;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * xml的RSA签名算法
 */
@Component
public class AlgXmlRSASignature extends AbstractAlgorithm {

	/**
	 * 日志定义
	 */
	private static Logger logger = LoggerFactory.getLogger("CERT-ALGO");

	/**
	 * RSA 算法名字
	 */
	private static String KEY_ALGO_NAME = "RSA";

	public static String DDD = "http://apache.org/xml/features/disallow-doctype-decl";

	static {
		org.apache.xml.security.Init.init();
	}

	/**
	 * XML签名
	 *
	 *
	 *
	 *
	 *
	 * 支持下列算法
	 * <ul>
	 *     <li>XMLSignature.ALGO_ID_SIGNATURE_RSA</li>
	 *     <li>XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1</li>
	 *     <li>XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256</li>
	 *     <li>XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384</li>
	 *     <li>XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512</li>
	 * </ul>
	 *
	 * <ul>
	 *   <li>作为子节点：  XmlSignatureAppendMode.AS_CHILDREN</li>
	 *   <li>作为兄弟节点：XmlSignatureAppendMode.AS_BROTHER</li>
	 * </ul>
	 *
	 * @throws Exception the exception
	 */
	@Override
	public byte[] signXmlElement(byte[] priKeyData, byte[] certData, byte[] xmlDocBytes,
								String encode, String elementTagName, String algorithm,
								int signatureAppendMode) {
		try {
			Document xmlDocument = getXmlDocument(xmlDocBytes, encode);
			XMLSignature xmlSignature = new XMLSignature(xmlDocument, xmlDocument.getDocumentURI(),
					algorithm);

			NodeList nodeList = xmlDocument.getElementsByTagName(elementTagName);
			if (nodeList == null || nodeList.getLength() - 1 < 0) {
				throw new Exception("Document element with tag name " + elementTagName
						+ " not fount");
			}

			Node elementNode = nodeList.item(0);
			if (elementNode == null) {
				throw new Exception("Document element with tag name " + elementTagName
						+ " not fount");
			}
			if (signatureAppendMode == XmlSignatureAppendMode.AS_CHILDREN) {
				elementNode.appendChild(xmlSignature.getElement());
			} else if (signatureAppendMode == XmlSignatureAppendMode.AS_BROTHER) {
				elementNode.getParentNode().appendChild(xmlSignature.getElement());
			} else {
				throw new IllegalArgumentException("Illegal Append Mode");
			}

			Transforms transforms = new Transforms(xmlDocument);
			transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
			xmlSignature.addDocument("", transforms, Constants.ALGO_ID_DIGEST_SHA1);
			//添加证书签发者信息
			appendIssuerSerial(xmlSignature, certData);
			PrivateKey privateKey = recoverPrivateKey(priKeyData);
			xmlSignature.sign(privateKey);

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			XMLUtils.outputDOM(xmlDocument, os);
			return os.toByteArray();
		} catch (Exception e) {
			LogUtil.error(logger, e, genLogSign(algorithm) + "sign error:");

			throw new CertificationException(CertificationErrorCode.SIGN_ERROR, genLogSign(algorithm), e);
		}
	}

	/**
	 * 签名添加证书信息
	 */
	private void appendIssuerSerial(XMLSignature xmlSignature, byte[] certData) {
		if (certData != null) {
			X509Certificate certificate = this.recoverCertificate(certData);
			X509Data x509data = new X509Data(xmlSignature.getDocument());
			x509data.addIssuerSerial(certificate.getIssuerDN().getName(), certificate
					.getSerialNumber());
			xmlSignature.getKeyInfo().add(x509data);
		}
	}

	/**
	 * 验证XML签名
	 *
	 * @throws Exception the exception
	 */
	@Override
	public boolean verifyXmlElement(byte[] pubKeyData, byte[] xmlDocBytes, String encode) {
		try {
			Document xmlDocument = getXmlDocument(xmlDocBytes, encode);
			NodeList signatureNodes = xmlDocument.getElementsByTagNameNS(Constants.SignatureSpecNS,
					"Signature");
			if (signatureNodes == null || signatureNodes.getLength() < 1) {
				throw new Exception("Signature element not found!");
			}

			Element signElement = (Element) signatureNodes.item(0);
			if (signElement == null) {
				throw new Exception("Signature element  not found");
			}

			XMLSignature signature = new XMLSignature(signElement, "");
			PublicKey publicKey = recoverPublicKey(pubKeyData);
			return signature.checkSignatureValue(publicKey);
		} catch (Exception e) {
			LogUtil.error(logger, e, genLogSign("xml algo") + "encrypt error:");

			throw new CertificationException(CertificationErrorCode.VERIFY_ERROR, genLogSign("xml algo"), e);
		}
	}

	/**
	 * 获取XML文件
	 *
	 * @throws Exception the exception
	 */
	public static Document getXmlDocument(byte[] xmlDocBytes, String encode) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature(DDD, true);
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource inputSource = new InputSource(new ByteArrayInputStream(xmlDocBytes));
		if (!StringUtils.isEmpty(encode)) {
			inputSource.setEncoding(encode);
		}
		return builder.parse(inputSource);
	}

	/**
	 * 恢复私钥
	 */
	public static PrivateKey recoverPrivateKey(byte[] data) {

		try {

			PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(data);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGO_NAME);
			return keyFactory.generatePrivate(pkcs8EncodedKeySpec);

		} catch (Exception e) {
			LogUtil.error(logger, e, genLogSign(KEY_ALGO_NAME) + "recover privateKey error:");

			throw new CertificationException(CertificationErrorCode.RECOVER_PRIVATE_KEY_ERROR,
					genLogSign(KEY_ALGO_NAME), e);
		}

	}

	/**
	 * 恢复公钥
	 */
	public static PublicKey recoverPublicKey(byte[] data) {

		try {
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(data);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGO_NAME);
			return keyFactory.generatePublic(keySpec);
		} catch (Exception e) {
			LogUtil.error(logger, e, genLogSign(KEY_ALGO_NAME) + "recover publicKey error:");

			throw new CertificationException(CertificationErrorCode.RECOVER_PUBLIC_KEY_ERROR,
					genLogSign(KEY_ALGO_NAME), e);
		}
	}

	/**
	 * 恢复证书
	 */
	public static X509Certificate recoverCertificate(byte[] data) {
		try {
			InputStream certIn = new ByteArrayInputStream(data);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return (X509Certificate) cf.generateCertificate(certIn);
		} catch (Exception e) {
			LogUtil.error(logger, e, genLogSign(KEY_ALGO_NAME) + "recover certificate error:");
			throw new CertificationException(CertificationErrorCode.RECOVER_KEY_ERROR,
					genLogSign(KEY_ALGO_NAME), e);
		}
	}

	/**
	 * 注册算法类到对应manager接口
	 */
	@Override
	protected void doRegisterManager() {
		XmlSignatureManager.registerAlgo(AlgorithmEnum.ALGO_ID_SIGNATURE_RSA, this);
		XmlSignatureManager.registerAlgo(AlgorithmEnum.ALGO_ID_SIGNATURE_RSA_SHA1, this);
		XmlSignatureManager.registerAlgo(AlgorithmEnum.ALGO_ID_SIGNATURE_RSA_SHA256, this);
		XmlSignatureManager.registerAlgo(AlgorithmEnum.ALGO_ID_SIGNATURE_RSA_SHA384, this);
		XmlSignatureManager.registerAlgo(AlgorithmEnum.ALGO_ID_SIGNATURE_RSA_SHA512, this);
	}
}
