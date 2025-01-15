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

import java.util.*;
import java.util.Map.Entry;

/**
 * 脚本分组信息，对比缓存已经存在的脚本信息，按照修改、新增、删除进行分组
 */
public class GroovyInfoGroup {

	/**
	 * 修改的脚本
	 */
	private final List<GroovyInfo> modifiedGroovy = new ArrayList<GroovyInfo>();

	/**
	 * 新增的脚本
	 */
	private final List<GroovyInfo> addedGroovy = new ArrayList<GroovyInfo>();

	/**
	 * 删除的脚本
	 */
	private final List<GroovyInfo> deletedGroovy = new ArrayList<GroovyInfo>();

	/**
	 *
	 */
	public GroovyInfoGroup(List<GroovyInfo> groovyInfos) {
		for (GroovyInfo groovyInfo : groovyInfos) {
			GroovyInfo cacheGroovy = GroovyInnerCache.getByName(groovyInfo.getClassName());

			// 缓存没有，标识新增的脚本
			if (cacheGroovy == null) {
				addedGroovy.add(groovyInfo);
				continue;
			}

			// 跟缓存的脚本代码不一致，标识修改的脚本
			if (!StringUtils.equals(cacheGroovy.getGroovyContent(), groovyInfo.getGroovyContent())) {
				modifiedGroovy.add(groovyInfo);
				continue;
			}
		}

		// 缓存中存在，新的列表不存在，说明已经删除
		Map<String, GroovyInfo> cacheGroovyInfos = GroovyInnerCache.getGroovyInfos();
		Set<Entry<String, GroovyInfo>> entrySet = cacheGroovyInfos.entrySet();
		for (Entry<String, GroovyInfo> entry : entrySet) {
			if (!groovyInfos.contains(entry.getValue())) {
				deletedGroovy.add(entry.getValue());
			}
		}
	}

	/**
	 * Getter method for property <tt>modifiedGroovy</tt>.
	 */
	public List<GroovyInfo> getModifiedGroovy() {
		return modifiedGroovy;
	}

	/**
	 * Getter method for property <tt>addedGroovy</tt>.
	 */
	public List<GroovyInfo> getAddedGroovy() {
		return addedGroovy;
	}

	/**
	 * Getter method for property <tt>deletedGroovy</tt>.
	 */
	public List<GroovyInfo> getDeletedGroovy() {
		return deletedGroovy;
	}

}
