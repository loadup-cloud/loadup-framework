package com.github.loadup.components.gateway.core.prototype.util;

import com.github.loadup.components.gateway.common.exception.ErrorCode;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.enums.InterfaceScope;
import com.github.loadup.components.gateway.core.common.enums.InterfaceType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

/**
 * metric log util class
 */
@Component
public class MetricLoggerUtil {

    /**
     * metric binder
     */
    private static String metricBinder;

    //    private static MetricBinding metricBinding;

    /**
     * print monitor metric info
     */
    public static void monitor(String url, long timeCost, boolean success, String clientId,
                               InterfaceType messageType, String traceId, InterfaceScope interfaceScope) {
        if (!judgeBinderEnabled()) {
            return;
        }

        LinkedHashMap<String, String> tagMap = new LinkedHashMap<>();
        tagMap.put(Constant.KEY_HTTP_CLIENT_ID, clientId);
        tagMap.put(Constant.MESSAGE_TYPE, messageType.getCode());

        //        Tag[] tags = buildTags(tagMap);
        //
        //        metricBinding.monitorApi(DigestLoggerUtil.DIGEST_HTTP_LOGGER, url, (double) timeCost,
        //            success, tags, traceId, interfaceScope.getCode());
    }

    /**
     * print monitor metric info
     */
    public static void countError(String url, long timeCost, String clientId,
                                  InterfaceType messageType, String traceId, ErrorCode errorCode) {
        if (!judgeBinderEnabled()) {
            return;
        }

        LinkedHashMap<String, String> tagMap = new LinkedHashMap<>();
        tagMap.put(Constant.KEY_HTTP_CLIENT_ID, clientId);
        tagMap.put(Constant.MESSAGE_TYPE, messageType.getCode());
        tagMap.put(Constant.ERROR_CODE, errorCode.getCode());

        //        Tag[] tags = buildTags(tagMap);
        //
        //        metricBinding.count(DigestLoggerUtil.COUNT_ERROR_LOGGER, url, (double) timeCost, tags,
        //            traceId);
    }

    /**
     * build tags from hash map


     */
    //    private static Tag[] buildTags(Map<String, String> tagMap) {
    //        Tag[] tags = new Tag[tagMap.size()];
    //        int i = 0;
    //        for (Map.Entry<String, String> entry : tagMap.entrySet()) {
    //            tags[i++] = new Tag(entry.getKey(), entry.getValue());
    //        }
    //        return tags;
    //    }

    /**
     * judge metric binder enabled or not
     */
    public static boolean judgeBinderEnabled() {
        return StringUtils.isNotBlank(metricBinder);
    }

    /**
     * Setter method for property <tt>metricBinder</tt>.
     */
    @Value("${spring.cloud.antfin.metric.bindings.monitor.binder-name:}")
    public void setMetricBinder(String metricBinder) {
        MetricLoggerUtil.metricBinder = metricBinder;
    }

    /**
     * Setter method for property <tt>metricBinding</tt>.
     *

     */
    //    @Resource(required = false)
    //    public void setMetricBinding(MetricBinding metricBinding) {
    //        MetricLoggerUtil.metricBinding = metricBinding;
    //    }

}