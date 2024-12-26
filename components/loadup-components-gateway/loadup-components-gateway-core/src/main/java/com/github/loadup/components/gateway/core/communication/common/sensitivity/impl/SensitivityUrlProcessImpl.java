package com.github.loadup.components.gateway.core.communication.common.sensitivity.impl;

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
 * 敏感URL数据处理
 */
@Component("sensitivityUrlProcessImpl")
public class SensitivityUrlProcessImpl implements SensitivityDataProcess<String> {

    private static final String MASK_PATTERN_VALUE_URI = "(%s)=(\\S+?)(&|$)";

    @Override
    public String mask(String maskContent, Map<String, ShieldType> shieldRule) {

        Map<ShieldType, List<String>> maskRules = shieldRule.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())));

        String result = maskContent;

        for (Map.Entry<ShieldType, List<String>> entry : maskRules.entrySet()) {

            for (String maskField : entry.getValue()) {
                String maskUriPatternStr = getMaskUriPatternStr(maskField);
                result = maskUrl(result, maskUriPatternStr, entry.getKey());
            }
        }
        return result;
    }

    private static String maskUrl(String url, String patternStr, ShieldType shieldType) {
        if (StringUtils.isBlank(patternStr)) {
            return url;
        }

        StringBuffer sb = new StringBuffer();
        Pattern p = Pattern.compile(patternStr);
        Matcher m = p.matcher(url);
        while (m.find()) {
            String value = m.group(2);
            if (StringUtils.isNotBlank(value)) {
                // 对value进行敏感处理
                String maskValue = MaskUtil.mask(value.trim(), shieldType.name());
                String oriContext = m.group(0);
                String replace = null;
                int offset = oriContext.lastIndexOf(value);
                String last = oriContext.substring(offset);
                replace = oriContext.substring(0, offset) + last.replace(value, maskValue);
                m.appendReplacement(sb, replace);

            } else {
                m.appendReplacement(sb, m.group(0));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String getMaskUriPatternStr(String maskRule) {

        if (StringUtils.isEmpty(maskRule)) {
            //没有配置敏感字段
            return StringUtils.EMPTY;
        }
        return String.format(MASK_PATTERN_VALUE_URI, maskRule);
    }

    @Override
    public SensitivityProcessType getTag() {
        return SensitivityProcessType.URL;
    }
}