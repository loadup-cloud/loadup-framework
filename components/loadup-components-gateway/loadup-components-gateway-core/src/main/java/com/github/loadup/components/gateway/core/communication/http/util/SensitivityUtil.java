package com.github.loadup.components.gateway.core.communication.http.util;

import com.github.loadup.components.gateway.core.communication.common.sensitivity.SensitivityDataProcess;
import com.github.loadup.components.gateway.core.model.SensitivityProcessType;
import com.github.loadup.components.gateway.core.model.ShieldType;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 敏感日志工具
 * 使用正则表达式匹配敏感字段
 * <p>
 * <p>
 * <p>
 * <p>
 * Exp $
 */
@Component
public class SensitivityUtil {

    protected static final Logger logger = LoggerFactory
            .getLogger(SensitivityUtil.class);

    private static Map<SensitivityProcessType, SensitivityDataProcess> sensitivityDataProcessMap;

    /**
     * XML header checker
     */
    public static final String XML_HEADER_CHECKER = "<";

    /**
     * JSON header checker
     */
    public static final String JSON_HEADER_CHECKER = "{";

    /**
     * FROM header checker
     */
    public static final String FORM_CHECKER = "=";

    /**
     * Setter method for property <tt>sensitivityDataProcessList</tt>.
     */
    @Resource
    public void setSensitivityDataProcessList(List<SensitivityDataProcess> sensitivityDataProcessList) {
        sensitivityDataProcessMap = sensitivityDataProcessList.stream()
                .collect(Collectors.toMap(SensitivityDataProcess::getTag, m -> m));
    }

    /**
     *
     */
    public static <T> T mask(T content, Map<String, ShieldType> rules,
                             SensitivityProcessType type) {
        if (MapUtils.isEmpty(rules) || type == null) {
            return content;
        }

        SensitivityDataProcess<T> sensitivityDataProcess = sensitivityDataProcessMap.get(type);
        if (sensitivityDataProcess == null) {
            return content;
        } else {
            return sensitivityDataProcess.mask(content, rules);
        }
    }

    /**
     * 获取uri脱敏的正则
     * /**
     * is xml
     */
    private static boolean isXml(String message) {
        //取出message前后的空格字符和换行符
        String fixedMessage = StringUtils.trim(message);
        return fixedMessage != null && fixedMessage.startsWith(XML_HEADER_CHECKER);
    }

    /**
     * is json
     */
    private static boolean isJson(String message) {
        //取出message前后的空格字符和换行符
        String fixedMessage = StringUtils.trim(message);
        return fixedMessage != null && fixedMessage.startsWith(JSON_HEADER_CHECKER);
    }

    /**
     * is form
     */
    private static boolean isForm(String message) {
        return StringUtils.contains(message, FORM_CHECKER);
    }

    /**
     * 根据报文内容匹配敏感数据处理单元
     */
    public static SensitivityProcessType matchProcessTypeByString(String message) {
        if (isXml(message)) {
            return SensitivityProcessType.XML_BODY;
        } else if (isJson(message)) {
            return SensitivityProcessType.JSON_BODY;
        }
        //      这里在判断完不是xml和json格式的消息体后，为了性能考虑，通过=号简单判断是否是form类型的消息体，后续脱敏时会通过正则表达式来进行匹配
        else if (isForm(message)) {
            return SensitivityProcessType.FORM_BODY;
        } else {
            return null;
        }
    }

}
