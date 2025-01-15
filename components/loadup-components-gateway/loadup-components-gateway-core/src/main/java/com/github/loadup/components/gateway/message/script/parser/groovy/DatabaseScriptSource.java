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

import org.springframework.scripting.ScriptSource;

import java.io.IOException;

/**
 * <P>copy from supergw,modified by gang.caogang
 * <p>
 * Groovy脚本数据库数据源，实现了spring的ScriptSource接口供spring来管理Groovy脚本<br>
 * 每次获取解析报文的Groovy脚本时，从已有的缓存 ParserCache 中读取。
 */
public final class DatabaseScriptSource implements ScriptSource {

	/**
	 * 脚本名称
	 */
	private String scriptName;

	/**
	 * 构造函数
	 */
	public DatabaseScriptSource(String scriptName) {
		this.scriptName = scriptName;
	}

	/**
	 * @see ScriptSource#getScriptAsString()
	 */
	public String getScriptAsString() throws IOException {
		// 从内部缓存获取
		GroovyInfo groovyInfo = GroovyInnerCache.getByName(scriptName);
		if (groovyInfo != null) {
			return groovyInfo.getGroovyContent();
		} else {
			return "";
		}
	}

	/**
	 * @see ScriptSource#isModified()
	 */
	public boolean isModified() {
		return false;
	}

	/**
	 * @see ScriptSource#suggestedClassName()
	 */
	public String suggestedClassName() {
		return org.springframework.util.StringUtils.stripFilenameExtension(this.scriptName);
	}

}
