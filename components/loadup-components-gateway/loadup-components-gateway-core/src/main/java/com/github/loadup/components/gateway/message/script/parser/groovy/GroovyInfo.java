package com.github.loadup.components.gateway.message.script.parser.groovy;

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
 * <p>
 * GROOVY脚本信息
 */
public class GroovyInfo {

	/**
	 * 类名
	 */
	private String className;

	/**
	 * 脚本内容
	 */
	private String groovyContent;

	/**
	 * Getter method for property <tt>className</tt>.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Setter method for property <tt>className</tt>.
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Getter method for property <tt>groovyContent</tt>.
	 */
	public String getGroovyContent() {
		return groovyContent;
	}

	/**
	 * Setter method for property <tt>groovyContent</tt>.
	 */
	public void setGroovyContent(String groovyContent) {
		this.groovyContent = groovyContent;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		return prime * result + ((className == null) ? 0 : className.hashCode());
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		GroovyInfo other = (GroovyInfo) obj;
		return StringUtils.equals(other.className, this.className);
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return "groovyInfo[className=" + className + "]";
	}

}
