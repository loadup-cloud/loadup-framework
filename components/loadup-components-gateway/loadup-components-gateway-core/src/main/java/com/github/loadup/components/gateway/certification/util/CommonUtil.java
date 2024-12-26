package com.github.loadup.components.gateway.certification.util;

import com.github.loadup.components.gateway.certification.model.CertificationFactor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 通用工具类
 */
public class CommonUtil {

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
     * 用方括号包住具体值
     */
    public static String decorateBySquareBrackets(String data) {
        return new StringBuilder().append(SQUARE_BRACKET_LEFT).append(data).append(
                SQUARE_BRACKET_RIGHT).toString();
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