package com.github.loadup.components.gateway.core.communication.common.sensitivity.impl;

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

import com.github.loadup.components.gateway.core.communication.common.sensitivity.SensitivityDataProcess;
import com.github.loadup.components.gateway.core.model.SensitivityProcessType;
import com.github.loadup.components.gateway.core.model.ShieldType;
import com.github.loadup.components.gateway.core.prototype.util.MaskUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 */
@Component("sensitivityXmlProcessImpl")
public class SensitivityXmlProcessImpl implements SensitivityDataProcess<String> {
	/**
	 * 默认的XML脱敏正则表达式
	 */
	private static final String MASK_PATTERN_VALUE_XML = "<(%s)>([\\s\\S]+?)</\\1>";

	@Override
	public String mask(String maskContent, Map<String, ShieldType> shieldRule) {

		Map<ShieldType, List<String>> maskRules = shieldRule.entrySet().stream()
				.collect(Collectors.groupingBy(Map.Entry::getValue,
						Collectors.mapping(Map.Entry::getKey, Collectors.toList())));

		String result = maskContent;
		for (Map.Entry<ShieldType, List<String>> entry : maskRules.entrySet()) {

			for (String maskField : entry.getValue()) {
				String maskUriPatternStr = getMaskPatternValueJson(maskField);
				result = mask(result, maskUriPatternStr, entry.getKey());
			}
		}
		return result;
	}

	private static String mask(String message, String patternStr, ShieldType shieldType) {

		if (StringUtils.isBlank(patternStr)) {
			return message;
		}

		StringBuffer sb = new StringBuffer();
		Pattern p = Pattern.compile(patternStr);
		Matcher m = p.matcher(message);
		while (m.find()) {
			String value = m.group(2);
			//兼容message为Json格式，且value为数字，无双引号包括的情况
			if (value == null && m.groupCount() == 3) {
				value = m.group(3);
			}
			if (StringUtils.isNotBlank(value)) {
				// 对value进行敏感处理
				String maskValue = MaskUtil.mask(value.trim(), shieldType.name());
				String oriContext = m.group(0);
				String replace = null;
				//json脱敏
				replace = oriContext.replace(">" + value + "<", ">" + maskValue + "<");
				m.appendReplacement(sb, replace);
			} else {
				m.appendReplacement(sb, m.group(0));
			}
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private static String getMaskPatternValueJson(String field) {
		return String.format(MASK_PATTERN_VALUE_XML, field);
	}

	@Override
	public SensitivityProcessType getTag() {
		return SensitivityProcessType.XML_BODY;
	}
}
