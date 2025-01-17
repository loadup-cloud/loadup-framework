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

import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.communication.common.util.CommunicationConfigUtil;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * HttpLogUtil.java
 * contains some http tools
 * </p>
 */
public class HttpToolUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpToolUtil.class);

    /**
     * Return the http response information
     */
    public static void returnResponse(HttpServletResponse httpResponse,
                                    MessageEnvelope messageResponse,
                                    CommunicationConfig requesterCommunicationConfig) {
        Map<String, String> resMap = messageResponse.getHeaders();
        String resResult = String.valueOf(messageResponse.getContent());
        try {
            //Header信息填写
            resMap.forEach((k, v) -> {
                if (StringUtils.isBlank(v) || StringUtils.equalsIgnoreCase("content-length", k)) {
                    LogUtil.warn(logger, k, " is empty, so can not set in the http header");
                } else {
                    httpResponse.addHeader(k, v);
                }
            });
            if (requesterCommunicationConfig != null) {
                String sendCharset = CommunicationConfigUtil
                        .getSendCharset(requesterCommunicationConfig);
                httpResponse.setCharacterEncoding(sendCharset);
            }
            //报文信息
            httpResponse.getWriter().write(resResult);
        } catch (Exception e) {
            ExceptionUtil.caught(e, "response exception", resResult);
        }
    }

    /**
     * 从HttpServletRequest中读取二进制格式的报文
     *
     * @throws IOException
     */
    public static byte[] getDataFromHttpRequest(HttpServletRequest request) throws IOException {
        // 从属性中直接获取已缓存的请求体
        byte[] content = (byte[]) request.getAttribute(Constant.REQUEST_BODY_KEY);
        if (content != null) {
            return content;
        }

        // 根据 Content-Type 判断处理方式
        if (StringUtils.contains(request.getContentType(), Constant.FORM_URL_ENCODED_CONTENT_TYPE)) {
            content = getContentFromParameterMap(request);
        } else {
            // 使用 try-with-resources 自动关闭流
            try (InputStream inputStream = request.getInputStream()) {
                if (inputStream == null) {
                    return new byte[0];
                }
                // 使用 Spring 提供的工具类
                content = StreamUtils.copyToByteArray(inputStream);
            }
        }

        // 缓存请求体到 request 属性中
        request.setAttribute(Constant.REQUEST_BODY_KEY, content);

        return content;
    }

    /**
     * get content bytes from request.getParameterMap
     */
    private static byte[] getContentFromParameterMap(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream().map(entry -> {
                    String[] value = entry.getValue();
                    if (value == null) {
                        return null;
                    } else if (value.length > 1) {
                        return Arrays.stream(value).map(s -> entry.getKey() + "=" + s)
                                .collect(Collectors.joining(Constant.URL_KEY_VALUE_SEPARATOR));
                    } else {
                        return entry.getKey() + "=" + value[0];
                    }
                }).filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(Constant.URL_KEY_VALUE_SEPARATOR)).getBytes();
    }

    /**
     * Gets get http header from htt request.
     */
    public static Map<String, String> getHeaderFromHttpRequest(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration headNames = request.getHeaderNames();
        for (Enumeration e = headNames; e.hasMoreElements(); ) {
            String headName = e.nextElement().toString();
            headers.put(headName, request.getHeader(headName));
        }
        return headers;
    }

    public static String getHeaderFromHttpRequest(HttpServletRequest request, String headerName) {
        Map<String, String> headers = getHeaderFromHttpRequest(request);
        return headers.getOrDefault(headerName, "");
    }

    /**
     * Gets get http header from htt method.
     */
    public static Map<String, String> getHttpHeaderFromHttMethod(HttpUriRequestBase httpMethod) {
        Map<String, String> headers = new HashMap<>();
        Arrays.stream(httpMethod.getHeaders()).forEach(header -> {
            headers.put(header.getName(), header.getValue());
        });

        return headers;
    }
}
