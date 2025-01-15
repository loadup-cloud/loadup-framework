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

/**
 * Groovy 相关常量
 */
public final class GroovyConstant {

	/**
	 * GROOVY 脚本配置标签
	 */
	public static final String SPRING_TAG = "lang:groovy";

	/**
	 * GROOVY 脚本在配置中的前缀
	 */
	public static final String SCRIPT_SOURCE_PREFIX = "database:";

	/**
	 * 配置文件开头
	 */
	public static final String BEANS_FILE_CONTENT = "<beans xmlns='http://www.springframework.org/schema/beans'\n"
			+ "    xmlns:lang='http://www.springframework.org/schema/lang'\n"
			+ "    xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n"
			+ "    xsi:schemaLocation='http://www.springframework.org/schema/beans http://www.springframework"
			+ ".org/schema/beans/spring-beans-2.5.xsd\n"
			+ "                        http://www.springframework.org/schema/lang http://www.springframework"
			+ ".org/schema/lang/spring-lang-2.5.xsd' default-autowire='byName'>"
			+ "</beans>\n";

	/**
	 * 禁用构造函数
	 */
	private GroovyConstant() {
		// 禁用构造函数
	}
}
