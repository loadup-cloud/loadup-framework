package com.github.loadup.components.gateway.core.model;

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

import org.apache.commons.lang3.StringUtils;

/**
 * 安全组件Groovy算法配置
 */
public class CertAlgoScript {

	/**
	 * 类名
	 */
	private String className;

	/**
	 * 算法脚本内容
	 */
	private String algoScriptContent;

	/**
	 * Getter method for property <tt>className<tt>.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Setter method for property <tt>className<tt>.
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Getter method for property <tt>algoScriptContent<tt>.
	 */
	public String getAlgoScriptContent() {
		return algoScriptContent;
	}

	/**
	 * Setter method for property <tt>algoScriptContent<tt>.
	 */
	public void setAlgoScriptContent(String algoScriptContent) {
		this.algoScriptContent = algoScriptContent;
	}

	/**
	 * 判断两个变量脚本内容是否相同
	 */
	public boolean sameScriptContent(CertAlgoScript another) {
		return StringUtils.equals(this.algoScriptContent, another.getAlgoScriptContent());
	}
}
