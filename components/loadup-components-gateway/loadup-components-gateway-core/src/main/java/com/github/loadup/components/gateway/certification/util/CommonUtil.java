/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.certification.util;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 通用工具类
 */
public class CommonUtil {

    /**
     * 配置分隔符
     */
    private static final Pattern CONFIG_SEPARATOR = Pattern.compile("\r*\n+|;");
    /**
     * semi colon
     */
    private static final String SEMI_COLON = ";";
    /**
     * 值/对分隔符
     */
    private static final String VALUE_SEPARATOR = "=";
    /**
     * key=valye;key2=value separator
     */
    private static final String SEMI_SEPARATOR = ";";
    /**
     * 左方括号
     */
    private static String SQUARE_BRACKET_LEFT = "[";
    /**
     * 右方括号
     */
    private static String SQUARE_BRACKET_RIGHT = "]";
    /**
     * 逗号
     */
    private static String COMMA = ",";

    /**
     * 用方括号包住具体值
     */
    public static String decorateBySquareBrackets(String data) {
        return new StringBuilder()
                .append(SQUARE_BRACKET_LEFT)
                .append(data)
                .append(SQUARE_BRACKET_RIGHT)
                .toString();
    }

    /**
     * 输出相关参数
     */
    public static String decorateBySquareBrackets(Object... obj) {
        StringBuilder rtnString = new StringBuilder();
        rtnString.append(SQUARE_BRACKET_LEFT);
        for (int i = 0; i < obj.length - 1; i++) {
            rtnString.append(obj[i]).append(COMMA);
        }
        rtnString.append(obj[obj.length - 1]).append(SQUARE_BRACKET_RIGHT);
        return rtnString.toString();
    }

    /**
     * 基于操作要素生成摘要信息，用于日志记录[instId-appId-operationType]
     */
    public static String generateOperationDigest(CertificationFactor certificationFactor) {
        return decorateBySquareBrackets(certificationFactor.getBizKey(), certificationFactor.getOperationType());
    }

    /**
     * 将a=b;c=d 格式的配置转换为Map格式
     */
    public static Map<String, String> Str2Kv(String strInput) {

        Map<String, String> configMap = new HashMap<String, String>();
        if (StringUtils.isEmpty(strInput)) {
            return configMap;
        }
        String[] configs = CONFIG_SEPARATOR.split(strInput);
        for (String config : configs) {
            int firstSplitorIndex = config.indexOf(VALUE_SEPARATOR);
            String keyString = StringUtils.substring(config, 0, firstSplitorIndex);
            keyString = StringUtils.trim(keyString);
            configMap.put(keyString, config.substring(firstSplitorIndex + 1));
        }

        return configMap;
    }

    /**
     * 将Map格式的配置转换为a=b;c=d格式
     */
    public static String kv2Str(Map<String, String> kvs) {

        if (MapUtils.isEmpty(kvs)) {
            return StringUtils.EMPTY;
        }
        List<String> strs = new ArrayList<>();
        for (Map.Entry<String, String> entry : kvs.entrySet()) {
            strs.add(entry.getKey() + VALUE_SEPARATOR + entry.getValue());
        }
        return StringUtils.join(strs, SEMI_SEPARATOR);
    }
}
