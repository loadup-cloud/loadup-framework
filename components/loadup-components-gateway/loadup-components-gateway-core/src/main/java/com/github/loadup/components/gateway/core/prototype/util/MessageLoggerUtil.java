/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.core.prototype.util;

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

import com.github.loadup.components.gateway.cache.common.SystemParameter;
import com.github.loadup.components.gateway.core.common.enums.InterfaceType;
import com.github.loadup.components.gateway.core.communication.http.util.SensitivityUtil;
import com.github.loadup.components.gateway.core.model.SensitivityProcessType;
import com.github.loadup.components.gateway.core.model.ShieldConfig;
import com.github.loadup.components.gateway.core.model.ShieldType;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * MessageLoggerUtil.java
 * </p>
 */
public class MessageLoggerUtil {
    /**
     * message digest
     */
    public static final Logger commonMessageLogger = LoggerFactory.getLogger("COMMON-MESSAGE-LOGGER");

    /**
     * Print http receive log.
     */
    public static void printHttpReceiveLog(
            String interfaceId,
            InterfaceType type,
            String uuid,
            String uri,
            String httpMethod,
            String message,
            Map<String, String> httpHeader) {

        printHttpLog(
                "httpclient receive " + type + " message",
                interfaceId,
                type,
                uuid,
                uri,
                httpMethod,
                message,
                httpHeader);
    }

    /**
     * Print http send log.
     */
    public static void printHttpSendLog(
            String interfaceId,
            InterfaceType type,
            String uuid,
            String url,
            String httpMethod,
            String message,
            Map<String, String> httpHeader) {

        printHttpLog(
                "httpclient send " + type + " message", interfaceId, type, uuid, url, httpMethod, message, httpHeader);
    }

    /**
     * Print http log.
     */
    public static void printHttpLog(
            String dialog,
            String interfaceId,
            InterfaceType interfaceType,
            String uuid,
            String url,
            String httpMethod,
            String message,
            Map<String, String> httpHeader) {

        try {

            Map<String, ShieldType> rules = ShieldConfig.getShieldRules(interfaceId, interfaceType);
            StringBuilder log = new StringBuilder();
            String maskUrl = SensitivityUtil.mask(url, rules, SensitivityProcessType.URL);
            log.append("[HTTP] ").append(dialog);
            log.append(",uuid:").append(uuid);
            log.append(",URL:").append(maskUrl);
            log.append(",http method:").append(httpMethod).append(",");

            if (MapUtils.isNotEmpty(httpHeader)) {

                Map<String, String> maskHeader =
                        SensitivityUtil.mask(httpHeader, rules, SensitivityProcessType.HEADER_MAP);

                maskHeader.forEach((k, v) -> {
                    log.append("HttpHeader:").append(k).append("=").append(v).append(",");
                });
            }
            log.append("message=");
            message = SensitivityUtil.mask(message, rules, SensitivityUtil.matchProcessTypeByString(message));
            message = getMaxLengthLog(message);
            // 去除换行
            if (StringUtils.isNotBlank(message)) {
                message = message.replaceAll("[\\t\\n\\r]", "");
            }
            log.append(message);
            LogUtil.info(commonMessageLogger, log.toString());
        } catch (Exception e) {
            // 任何异常不再抛出
            ExceptionUtil.caught(e, "print exception log .");
        }
    }

    public static String getMaxLengthLog(String message) {
        try {
            String maxLogLength = SystemParameter.getParameter("maxLogLength");
            int maxLength = Integer.parseInt(maxLogLength);
            if (message.length() > maxLength) {
                message = StringUtils.substring(message, 0, maxLength) + "...";
            }
        } catch (Exception e) {
            ExceptionUtil.caught(e, "getMaxLengthLog catch exception, " + e.getMessage());
        }

        return message;
    }
}
