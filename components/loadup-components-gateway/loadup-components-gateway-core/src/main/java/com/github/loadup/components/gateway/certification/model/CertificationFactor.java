package com.github.loadup.components.gateway.certification.model;

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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;

/**
 * 加解密要素
 */
public class CertificationFactor {

	/**
	 * 业务Key
	 */
	private String bizKey;

	/**
	 * 操作类型
	 */
	private OperationType operationType;

	/**
	 * 算法的String描述
	 */
	private String algoString;

	/**
	 * 算法参数
	 */
	private Map<String, String> algoParameter;

	/**
	 * 需要证书列表
	 */
	private Map<String, Object> certMap;

	/**
	 * 证书别名列表
	 */
	private Map<String, String> certAliasNameMap;

	/**
	 * Getter method for property <tt>algoParameter<tt>.
	 */
	public Map<String, String> getAlgoParameter() {
		return algoParameter;
	}

	/**
	 * Setter method for property <tt>algoParameter<tt>.
	 */
	public void setAlgoParameter(Map<String, String> algoParameter) {
		this.algoParameter = algoParameter;
	}

	/**
	 * Getter method for property <tt>algoString<tt>.
	 */
	public String getAlgoString() {
		return algoString;
	}

	/**
	 * Setter method for property <tt>algoString<tt>.
	 */
	public void setAlgoString(String algoString) {
		this.algoString = algoString;
	}

	/**
	 * Getter method for property <tt>bizKey<tt>.
	 */
	public String getBizKey() {
		return bizKey;
	}

	/**
	 * Setter method for property <tt>bizKey<tt>.
	 */
	public void setBizKey(String bizKey) {
		this.bizKey = bizKey;
	}

	/**
	 * Getter method for property <tt>certMap<tt>.
	 */
	public Map<String, Object> getCertMap() {
		return certMap;
	}

	/**
	 * Setter method for property <tt>certMap<tt>.
	 */
	public void setCertMap(Map<String, Object> certMap) {
		this.certMap = certMap;
	}

	/**
	 * Getter method for property <tt>operationType<tt>.
	 */
	public OperationType getOperationType() {
		return operationType;
	}

	/**
	 * Setter method for property <tt>operationType<tt>.
	 */
	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	/**
	 * Getter method for property <tt>certAliasNameMap<tt>.
	 */
	public Map<String, String> getCertAliasNameMap() {
		return certAliasNameMap;
	}

	/**
	 * Setter method for property <tt>certAliasNameMap<tt>.
	 */
	public void setCertAliasNameMap(Map<String, String> certAliasNameMap) {
		this.certAliasNameMap = certAliasNameMap;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);

	}
}
