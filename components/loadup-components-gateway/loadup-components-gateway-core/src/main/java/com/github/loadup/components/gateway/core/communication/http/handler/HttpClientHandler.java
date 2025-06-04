/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.core.communication.http.handler;

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
import com.github.loadup.commons.error.CommonException;
import com.github.loadup.commons.result.ResultCode;
import com.github.loadup.components.gateway.cache.InterfaceConfigCache;
import com.github.loadup.components.gateway.common.exception.util.AssertUtil;
import com.github.loadup.components.gateway.common.util.CommonUtil;
import com.github.loadup.components.gateway.core.common.enums.InterfaceType;
import com.github.loadup.components.gateway.core.common.enums.MessageFormat;
import com.github.loadup.components.gateway.core.common.enums.RoleType;
import com.github.loadup.components.gateway.core.communication.common.code.CommunicationErrorCode;
import com.github.loadup.components.gateway.core.communication.common.model.CommunicationProperty;
import com.github.loadup.components.gateway.core.communication.common.util.CommunicationConfigUtil;
import com.github.loadup.components.gateway.core.communication.http.cache.HttpClientCache;
import com.github.loadup.components.gateway.core.communication.http.util.HttpConfigUtil;
import com.github.loadup.components.gateway.core.ctrl.RuntimeProcessContextHolder;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.model.InterfaceConfig;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.core.model.communication.TransportURI;
import com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants;
import com.github.loadup.components.gateway.core.prototype.constant.RestMethod;
import com.github.loadup.components.gateway.core.prototype.util.DigestLoggerUtil;
import com.github.loadup.components.gateway.core.prototype.util.ExceptionUtil;
import com.github.loadup.components.gateway.core.prototype.util.HttpToolUtil;
import com.github.loadup.components.gateway.core.prototype.util.MessageLoggerUtil;
import com.github.loadup.components.gateway.facade.util.IOUtils;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import jakarta.annotation.Resource;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Http客户端处理类
 */
@Component
public class HttpClientHandler {

    /**
     * http的statuscode
     */
    public static final String HTTP_STATUS_CODE = "_httpStatusCode";
    /**
     * logger
     */
    private static final Logger logger = DigestLoggerUtil.digestMessageLogger;
    /**
     * 保存http内容的key
     **/
    private static final String HTTP_CONTENT = "_httpContent";
    /**
     * http客户端缓存
     */
    @Resource
    private HttpClientCache httpClientCache;

    /**
     * common log message, mask off sensitive data
     */
    public static void printHttpRecvLog(
            String interfaceId, String uuid, HttpUriRequestBase httpMethod, MessageEnvelope messageEnvelope) {

        Map<String, String> httpHeader = messageEnvelope.getHeaders();
        try {
            String uri = httpMethod.getUri().toString();
            String message = CommonUtil.getMsgContent(messageEnvelope);
            MessageLoggerUtil.printHttpReceiveLog(
                    interfaceId, InterfaceType.SPI, uuid, uri, httpMethod.getMethod(), message, httpHeader);
        } catch (Exception e) {
            ExceptionUtil.caught(e, "print exception log");
        }
    }

    /**
     * http发送处理
     *
     * @throws HttpException
     * @throws IOException
     */
    public MessageEnvelope handler(
            String transUUID, CommunicationConfig communicationConfig, MessageEnvelope messageEnvelope)
            throws Exception, IOException {
        CloseableHttpClient client = httpClientCache.getClient(communicationConfig);

        AssertUtil.isNotNull(client, CommunicationErrorCode.CLIENT_ERROR);

        HttpUriRequestBase httpMethod = createHttpMethod(communicationConfig);

        constructParams(messageEnvelope, httpMethod, communicationConfig);

        printHttpSendLog(communicationConfig.getInterfaceId(), transUUID, httpMethod, messageEnvelope);

        Object resultContent = runTask(client, httpMethod, communicationConfig);

        MessageEnvelope result = this.convertInMessage(resultContent, httpMethod, communicationConfig);

        printHttpRecvLog(communicationConfig.getInterfaceId(), transUUID, httpMethod, result);

        return result;
    }

    /**
     * build http method
     */
    public HttpUriRequestBase createHttpMethod(CommunicationConfig config) {
        HttpUriRequestBase result = null;
        TransportURI url = config.getUri();

        String httpReqType = HttpConfigUtil.getHttpType(config);
        if (StringUtils.equalsIgnoreCase(httpReqType, RestMethod.POST)) {
            result = new HttpPost(url.getUrl());
        } else if (StringUtils.equalsIgnoreCase(httpReqType, RestMethod.GET)) {
            result = new HttpGet(url.getUrl());
        } else if (StringUtils.equalsIgnoreCase(httpReqType, RestMethod.DELETE)) {
            result = new HttpDelete(url.getUrl());
        } else if (StringUtils.equalsIgnoreCase(httpReqType, RestMethod.PATCH)) {
            result = new HttpPatch(url.getUrl());
        } else {
            LogUtil.warn(logger, "unsupported HTTP request type:", httpReqType);
        }
        return result;
    }

    /**
     * assemble http parameter
     *
     * @throws UnsupportedEncodingException
     */
    @SuppressWarnings("unchecked")
    public void constructParams(
            MessageEnvelope outMessage, HttpUriRequestBase httpMethod, CommunicationConfig communicationConfig)
            throws UnsupportedEncodingException {
        Map<String, String> headers = outMessage.getHeaders();
        Map<String, String> params = null;
        String content = null;
        switch (outMessage.getMessageFormat()) {
            case MAP: {
                params = (Map<String, String>) outMessage.getContent();
                break;
            }
            case TEXT:
            default: {
                content = (String) outMessage.getContent();
                break;
            }
        }

        // 添加请求参数和报文头
        addParameter(params, httpMethod);
        addRequestHeader(headers, httpMethod);

        if (httpMethod instanceof HttpPost) {
            //  只有POST请求才添加报文正文
            addContent(content, communicationConfig, httpMethod);
        }
    }

    /**
     * 运行发送任务
     *
     * @throws HttpException
     * @throws IOException
     */
    public Object runTask(
            final CloseableHttpClient client, final HttpUriRequestBase httpMethod, final CommunicationConfig config)
            throws Exception, IOException {

        try {
            String str = client.execute(httpMethod, res -> {
                int statusCode = res.getCode();
                String result = null;
                if (needConvertToErrorMessage(statusCode, config)) {
                    return convertErrorResponseBody(config.getHttpStatusErrorCode(statusCode));
                }
                if (!CommunicationConfigUtil.isNeedResponse(config)
                        && statusCode != 200
                        && !HttpConfigUtil.isRest(config)) {
                    LogUtil.warn(logger, "HTTP status code error ,statusCode=", res, config);
                    throw new CommonException(
                            CommunicationErrorCode.HTTP_STATUS_ERROR, "HTTP status code error ,statusCode=" + res);
                }
                if (CommunicationConfigUtil.isNeedResponse(config)) {
                    result = EntityUtils.toString(res.getEntity());
                }
                return result;
            });

            // 是否需要返回

            return str;
        } finally {
            // 释放连接，实际上httpclient会做一个连接复用，并不是真正的的关闭
            //            client.close(CloseMode.GRACEFUL);
            //            if (HttpConfigUtil.isHttpShortConnection(config)) {
            //                client.getHttpConnectionManager().closeIdleConnections(0);
            //            }
        }
    }

    /**
     * judge whether need convert to error message
     */
    private boolean needConvertToErrorMessage(int statusCode, CommunicationConfig receiverCommunicationConfig) {
        return statusCode != 200 && receiverCommunicationConfig.getHttpStatusErrorCode(statusCode) != null;
    }

    /**
     * convert errorcode to string response message
     */
    private String convertErrorResponseBody(ResultCode errorCode) {
        JSONObject result = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", errorCode.getCode());
        jsonObject.put("message", errorCode.getMessage());
        jsonObject.put("status", errorCode.getStatus());
        result.put("result", jsonObject);
        return result.toJSONString();
    }

    /**
     * convert input message
     *
     * @throws Exception
     */
    public MessageEnvelope convertInMessage(
            Object inMessage, HttpUriRequestBase httpMethod, CommunicationConfig receiverCommunicationConfig)
            throws IOException {

        // if there is no need to response, return it directly
        if (!CommunicationConfigUtil.isNeedResponse(receiverCommunicationConfig)) {
            return null;
        }

        MessageFormat format = receiverCommunicationConfig.getRecvMessageFormat();
        Object content = null;

        // set http header
        Map<String, String> responseHeaders = new HashMap<String, String>();
        //        for (Header header : httpMethod.getResponseHeaders()) {
        //            if (header.getName() != null) {
        //                responseHeaders.put(header.getName(), header.getValue());
        //            }
        //        }
        // set cert_code and integration url from http request header
        responseHeaders.put(ProcessConstants.KEY_REQUEST_CERT_CODE, getCertCode(receiverCommunicationConfig));
        responseHeaders.put(ProcessConstants.KEY_REQUEST_HTTP_INTEGRATION_URL, receiverCommunicationConfig.getUrl());

        if (format == MessageFormat.BYTE) {
            content = inMessage;
        } else if (format == MessageFormat.TEXT) {
            content = inMessage.toString();
            //            if (needConvertToErrorMessage(httpMethod, receiverCommunicationConfig)) {
            //                content = inMessage;
            //            } else {
            //                content = getStringByCharset(httpMethod.getResponseBodyAsStream(),
            //                        CommunicationConfigUtil.getRecvCharset(receiverCommunicationConfig),
            //                        CommunicationConfigUtil.getReceiveBufferSize(receiverCommunicationConfig));
            //            }
        } else if (format == MessageFormat.MAP) {
            // if the http return the map format, it will return the whole information
            //            Header[] headers = httpMethod.getResponseHeaders();
            //            Header[] foots = httpMethod.getResponseFooters();
            //            String mapContent = getStringByCharset(httpMethod.getResponseBodyAsStream(),
            //                    CommunicationConfigUtil.getRecvCharset(receiverCommunicationConfig),
            //                    CommunicationConfigUtil.getReceiveBufferSize(receiverCommunicationConfig));
            //            Map<String, String> map = new HashMap<String, String>();
            //            for (Header h : headers) {
            //                map.put(h.getName(), h.getValue());
            //            }
            //            for (Header h : foots) {
            //                map.put(h.getName(), h.getValue());
            //            }
            //            map.put(HTTP_CONTENT, mapContent);
            //            content = map;
        }

        MessageEnvelope responseMsg = new MessageEnvelope(format, content, responseHeaders);

        // if we need return the HTTP status, this will return the HTTP status
        if (HttpConfigUtil.needHttpStatusCode(receiverCommunicationConfig)) {
            //            responseMsg.getHeaders().put(HTTP_STATUS_CODE,
            //                    String.valueOf(httpMethod.getStatusCode()));
        }

        responseMsg.putExtMap(ProcessConstants.KEY_HTTP_METHOD, httpMethod.getMethod());
        responseMsg.putExtMap(ProcessConstants.KEY_HTTP_REQUEST_URI, httpMethod.getPath());

        return responseMsg;
    }

    /**
     * get security strategy code
     */
    private String getCertCode(CommunicationConfig reveiverCommunicationConfig) {
        InterfaceConfig receiverInterfaceConfig = InterfaceConfigCache.getWithInterfaceId(
                reveiverCommunicationConfig.getInterfaceId(),
                RoleType.RECEIVER,
                RuntimeProcessContextHolder.getRuntimeProcessContext().getTransactionType());
        return receiverInterfaceConfig.getSecurityStrategyCode();
    }

    /**
     * add request parameter
     */
    protected void addParameter(Map<String, String> params, HttpUriRequestBase method) {
        if (CollectionUtils.isEmpty(params)) {
            return;
        }

        if (method instanceof HttpPost) {
            // 对POST请求，直接添加参数

            List<NameValuePair> nvps = new ArrayList<>();
            // POST 请求参数
            for (Entry<String, String> entry : params.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            method.setEntity(new UrlEncodedFormEntity(nvps));

        } else if (method instanceof HttpGet) {
            // 对GET请求，需要将参数添加的url中
            StringBuilder queryString = new StringBuilder();
            // 表单参数
            List<NameValuePair> nvps = new ArrayList<>();
            // GET 请求参数
            nvps.add(new BasicNameValuePair("username", "wdbyte.com"));
            nvps.add(new BasicNameValuePair("password", "secret"));
            // 添加map中的所有参数
            for (Iterator<Entry<String, String>> iterator = params.entrySet().iterator(); iterator.hasNext(); ) {
                Entry<String, String> entry = iterator.next();
                queryString.append(entry.getKey()).append("=").append(entry.getValue());
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            URI uri = null;
            try {
                uri = new URIBuilder(method.getUri()).addParameters(nvps).build();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            method.setUri(uri);
        }
    }

    /**
     * add http header
     */
    protected void addRequestHeader(Map<String, String> headers, HttpUriRequestBase method) {
        if (CollectionUtils.isEmpty(headers)) {
            return;
        }

        Set<Entry<String, String>> entries = headers.entrySet();
        for (Entry<String, String> entry : entries) {
            method.addHeader(entry.getKey(), entry.getValue());
        }

        // 出口流量打上压测标识
        //        if (IfcLoadTestUtil.isLoadTestMode()) {
        //            method.addRequestHeader(LoadTestConstants.MARK_UID, LoadTestConstants.UID_VALUE);
        //        }
    }

    /**
     * add request information
     *
     * @throws UnsupportedEncodingException
     */
    protected void addContent(String content, CommunicationConfig communicationConfig, HttpUriRequestBase method)
            throws UnsupportedEncodingException {
        if (StringUtils.isBlank(content)) {
            return;
        }
        String contentMethod = communicationConfig.getProperty(CommunicationProperty.MESSAGE_FORMAT);

        if (StringUtils.equals(contentMethod, "byte")) {
            method.setEntity(
                    new ByteArrayEntity(java.util.Base64.getDecoder().decode(content), ContentType.APPLICATION_JSON));
        } else {
            String sendCharset = CommunicationConfigUtil.getSendCharset(communicationConfig);
            if (StringUtils.isNotBlank(sendCharset)) {
                method.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
            } else {
                method.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON, sendCharset, false));
            }
        }
    }

    /**
     * read string by charset
     *
     * @throws IOException
     */
    protected String getStringByCharset(InputStream content, String charset, int bufferSize) throws IOException {
        if (null == content) {
            return StringUtils.EMPTY;
        }
        Reader br = new InputStreamReader(content, charset);
        try {
            return IOUtils.readText(br, bufferSize);
        } finally {
            org.apache.commons.io.IOUtils.closeQuietly(br);
        }
    }

    /**
     * print http send log
     */
    public void printHttpSendLog(
            String interfaceId, String uuid, HttpUriRequestBase httpMethod, MessageEnvelope outMessage) {
        String url;
        String message;
        String method = httpMethod.getMethod();
        Map<String, String> httpHeader = HttpToolUtil.getHttpHeaderFromHttMethod(httpMethod);
        try {
            url = httpMethod.getUri().toString();
            message = CommonUtil.getMsgContent(outMessage);
            MessageLoggerUtil.printHttpSendLog(interfaceId, InterfaceType.SPI, uuid, url, method, message, httpHeader);
        } catch (Exception e) {
            // 任何异常不再抛出
            ExceptionUtil.caught(e, "print exception log .");
        }
    }
}
