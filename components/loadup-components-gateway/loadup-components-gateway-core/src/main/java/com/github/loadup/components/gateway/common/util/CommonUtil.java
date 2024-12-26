package com.github.loadup.components.gateway.common.util;

import com.alibaba.fastjson2.JSONObject;
import com.github.loadup.components.gateway.common.exception.ErrorCode;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.UUID;

/**
 * use to generate the common message, such as the message and log message
 */
public class CommonUtil {

    /**
     * 组装错误返回信息
     */
    public static String generateErrorResponse(String code, String message, String status) {

        JSONObject resultInfo = new JSONObject();
        resultInfo.put("code", code);
        resultInfo.put("message", message);
        resultInfo.put("status", status);

        JSONObject result = new JSONObject();
        result.put("result", resultInfo);
        return resultInfo.toJSONString();
    }

    /**
     * get content from message envelop
     */
    public static String getMsgContent(MessageEnvelope envelope) {
        if (envelope != null) {
            return getMsgLogContent(envelope);
        }
        return StringUtils.EMPTY;
    }

    /**
     * get message log content
     */
    private static String getMsgLogContent(MessageEnvelope envelope) {
        String logContent = envelope.getLogContent();
        if (StringUtils.isBlank(logContent)) {
            logContent = String.valueOf(envelope.getContent());
        }
        return logContent;
    }

    /**
     * generate traceId
     */
    public static String generateTraceId() {
        return Calendar.getInstance().getTimeInMillis()
                + UUID.randomUUID().toString();
    }

    /**
     * Generate the
     */
    public static JSONObject assembleAcResult(ErrorCode errorCode) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", errorCode.getCode());
        jsonObject.put("message", errorCode.getMessage());
        //jsonObject.put("status", errorCode.getStatus());

        return jsonObject;
    }

    /**
     * Generate ac format result
     */
    public static JSONObject assembleAcResultWhenException(ErrorCode errorCode) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("resultCode", errorCode.getCode());
        jsonObject.put("resultMessage", errorCode.getMessage());
        jsonObject.put("resultStatus", errorCode.getStatus());

        return jsonObject;
    }

}