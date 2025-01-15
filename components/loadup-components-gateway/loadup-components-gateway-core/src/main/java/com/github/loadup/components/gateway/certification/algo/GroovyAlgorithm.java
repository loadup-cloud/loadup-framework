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

import com.github.loadup.components.gateway.certification.model.CertificationFactor;

/**
 * groovy 算法接口
 */
public interface GroovyAlgorithm {

	/**
	 * 公共签名接口
	 */
	public String sign(String data, CertificationFactor certificationFactor);

	/**
	 * 公共验签接口
	 */
	public boolean verify(String unSignedData, String signedData,
						CertificationFactor certificationFactor);

	/**
	 * 加密操作接口
	 */
	public String encrypt(String data, CertificationFactor certificationFactor);

	/**
	 * 解密操作接口
	 */
	public String decrypt(String data, CertificationFactor certificationFactor);

	/**
	 * 有秘钥摘要算法
	 */
	public String digest(String data, CertificationFactor certificationFactor);

	/**
	 * 特殊签名算法
	 */
	public String encode(String srcContent, CertificationFactor certificationFactor);

	/**
	 * 特殊验签算法
	 */
	public String decode(String srcContent, CertificationFactor certificationFactor);

}
