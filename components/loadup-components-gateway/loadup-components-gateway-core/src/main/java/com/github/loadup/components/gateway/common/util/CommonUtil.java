package com.github.loadup.components.gateway.common.util;

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

import com.alibaba.fastjson2.JSONObject;
import com.github.loadup.commons.result.ResultCode;
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
    public static JSONObject assembleAcResult(ResultCode errorCode) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", errorCode.getCode());
        jsonObject.put("message", errorCode.getMessage());
        //jsonObject.put("status", errorCode.getStatus());

        return jsonObject;
    }

    /**
     * Generate ac format result
     */
    public static JSONObject assembleAcResultWhenException(ResultCode errorCode) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("resultCode", errorCode.getCode());
        jsonObject.put("resultMessage", errorCode.getMessage());
        jsonObject.put("resultStatus", errorCode.getStatus());

        return jsonObject;
    }

}
