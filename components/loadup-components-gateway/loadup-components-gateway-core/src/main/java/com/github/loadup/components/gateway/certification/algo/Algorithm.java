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

/**
 * 算法接口
 */
public interface Algorithm {

	/**
	 * 无秘钥摘要算法
	 */
	public byte[] digest(byte[] data, String algorithm);

	/**
	 * 有秘钥摘要算法
	 */
	public byte[] digest(byte[] data, byte[] key, String algorithm);

	/**
	 * 加密操作接口
	 */
	public byte[] encrypt(byte[] data, byte[] key, String algorithm);

	/**
	 * 解密操作接口
	 */
	public byte[] decrypt(byte[] data, byte[] key, String algorithm);

	/**
	 * 公共加密接口，带有秘钥类型
	 */
	public byte[] encrypt(byte[] data, byte[] key, String algorithm, boolean isPrivateKey);

	/**
	 * 公共解密接口， 带有秘钥类型
	 */
	public byte[] decrypt(byte[] data, byte[] key, String algorithm, boolean isPrivateKey);

	/**
	 * 公共签名接口
	 */
	public byte[] sign(byte[] data, byte[] key, String algorithm);

	/**
	 * 公共签名接口
	 */
	public byte[] sign(byte[] data, byte[] key, byte[] cert, String algorithm, boolean attach);

	/**
	 * 公共验签接口
	 */
	public boolean verify(byte[] unSignedData, byte[] signedData, byte[] key, String algorithm);

	/**
	 * 公共验签接口
	 */
	public boolean verify(byte[] unSignedData, byte[] signedData, byte[] key, String algorithm,
						boolean attach);

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
	public byte[] signXmlElement(byte[] priKeyData, byte[] certData, byte[] xmlDocBytes,
								String encode, String elementTagName, String algorithm,
								int signatureAppendMode);

	/**
	 * 验证XML签名
	 *
	 * @throws Exception the exception
	 */
	public boolean verifyXmlElement(byte[] pubKeyData, byte[] xmlDocBytes, String encode);

	/**
	 * KATONG等特殊签名算法
	 */
	public byte[] encode(byte[] privateKey, byte[] srcContent);

	/**
	 * KATONG等特殊验签算法
	 */
	public String decode(byte[] publicKey, byte[] srcContent);

}
