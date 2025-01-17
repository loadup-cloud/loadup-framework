package com.github.loadup.components.gateway.core.communication.http.impl;

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

import com.github.loadup.commons.error.CommonException;
import com.github.loadup.commons.result.ResultCode;
import com.github.loadup.components.gateway.common.exception.util.AssertUtil;
import com.github.loadup.components.gateway.core.common.enums.InterfaceScope;
import com.github.loadup.components.gateway.core.common.enums.InterfaceType;
import com.github.loadup.components.gateway.core.communication.common.code.CommunicationErrorCode;
import com.github.loadup.components.gateway.core.communication.common.facade.CommunicationService;
import com.github.loadup.components.gateway.core.communication.common.impl.CommunicationServiceImpl;
import com.github.loadup.components.gateway.core.communication.common.model.MessageSendResult;
import com.github.loadup.components.gateway.core.communication.http.cache.HttpClientCache;
import com.github.loadup.components.gateway.core.communication.http.handler.HttpClientHandler;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.core.model.communication.TransportProtocol;
import com.github.loadup.components.gateway.core.prototype.util.DigestLoggerUtil;
import com.github.loadup.components.gateway.core.prototype.util.ExceptionUtil;
import com.github.loadup.components.gateway.core.prototype.util.MetricLoggerUtil;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * HTTP客户端服务实现
 */
@Component("httpClientServiceImpl")
public class HttpClientServiceImpl implements CommunicationService, InitializingBean {

    /**
     * client-id is dispatch to the merchant
     */
    public static final String KEY_HTTP_CLIENT_ID = "client-id";
    /**
     * 连接拒绝
     */
    public static final String CONNECT_REFUSED = "Connection refused";
    /**
     * 连接超时
     */
    public static final String CONNECT_TIME_OUT = "Connection timed out";
    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(HttpClientServiceImpl.class);
    /**
     * http处理类
     */
    @Resource
    private HttpClientHandler httpClientHandler;

    /**
     * http客户端缓存
     */
    @Resource
    private HttpClientCache httpClientCache;

    /**
     * instproxy处理类
     */
    //    @Resource
    //    private InstproxyHandler    instproxyHandler;
    @Override
    public MessageSendResult send(String traceId, CommunicationConfig communicationConfig, MessageEnvelope messageEnvelope) {
        AssertUtil.isNotNull(communicationConfig, CommunicationErrorCode.CONFIG_ERROR);
        //if (HttpConfigUtil.isRest(conmunicationConfig)) {
        //    String params = String.valueOf(messageEnvelope.getContent());
        //    String url = conmunicationConfig.getUri().getUrl();
        //    url = url.concat(params);
        //    String schema = conmunicationConfig.getUri().getSchema();
        //    TransportURI transportURI = new TransportURI(url, schema);
        //    conmunicationConfig.setUri(transportURI);
        //
        //}
        boolean timeout = false;
        MessageEnvelope handlerResult = null;
        String protocol = communicationConfig.getProtocol();
        long timecost = System.currentTimeMillis();
        String callRespMsg = null;
        boolean success = false;
        ResultCode errorCode = null;
        try {
            if (StringUtils.equals(TransportProtocol.INSTPROXY, protocol.toUpperCase())) {
                //                handlerResult = instproxyHandler.handler(traceId, communicationConfig,messageEnvelope);
            } else {
                handlerResult = httpClientHandler.handler(traceId, communicationConfig, messageEnvelope);
            }
            callRespMsg = "SUCCESS";
            success = true;
        } catch (ConnectTimeoutException e) {
            callRespMsg = e.getMessage();
            ExceptionUtil.caught(e, "HttpClient send timeout:", communicationConfig);
            errorCode = CommunicationErrorCode.CONNECT_TIME_OUT;
            throw new CommonException(errorCode, e);
        } catch (SocketTimeoutException e) {
            timeout = true;
            callRespMsg = e.getMessage();
            ExceptionUtil.caught(e, "HttpClient send timeout:", communicationConfig);
            errorCode = CommunicationErrorCode.SOCKET_TIME_OUT;
            throw new CommonException(errorCode, e);
        } catch (ConnectException e) {
            callRespMsg = e.getMessage();
            ExceptionUtil.caught(e, "HttpClient connect exception:", communicationConfig);
            if (StringUtils.equalsIgnoreCase(e.getMessage(), CONNECT_TIME_OUT)) {
                errorCode = CommunicationErrorCode.CONNECT_TIME_OUT;
                throw new CommonException(errorCode, e);
            } else if (StringUtils.equalsIgnoreCase(e.getMessage(), CONNECT_REFUSED)) {
                errorCode = CommunicationErrorCode.CONNECT_REFUSED;
                throw new CommonException(errorCode, e);
            }
            errorCode = CommunicationErrorCode.IO_EXCEPTION;
            throw new CommonException(errorCode, e);
        } catch (CommonException e) {
            callRespMsg = e.getMessage();
            errorCode = e.getResultCode();
            throw e;
        } catch (Exception e) {
            callRespMsg = e.getMessage();
            errorCode = CommunicationErrorCode.IO_EXCEPTION;
            throw new CommonException(errorCode, e);
        } finally {
            long time = System.currentTimeMillis() - timecost;
            if (!MetricLoggerUtil.judgeBinderEnabled()) {
                DigestLoggerUtil.printSimpleDigestLog(communicationConfig.getUrl(), callRespMsg, time);
            } else {
                String clientId = messageEnvelope.getHeaders().get(KEY_HTTP_CLIENT_ID);
                MetricLoggerUtil.monitor(communicationConfig.getUrl(), time, success, clientId, InterfaceType.SPI,
                        traceId, InterfaceScope.OUTBOUND);
                if (!success) {
                    MetricLoggerUtil.countError(communicationConfig.getUrl(), time, clientId, InterfaceType.SPI, traceId, errorCode);
                }
            }
        }
        return new MessageSendResult(handlerResult, timeout, handlerResult != null);
    }

    @Override
    public void init(Object... obj) {
        LogUtil.info(logger, "begin to initialize http client cache");

        if (obj == null || obj.length == 0) {
            LogUtil.warn(logger, "parameters passed is null or empty!");
            return;
        }
        List<CommunicationConfig> communicationConfigs = (List<CommunicationConfig>) obj[0];
        httpClientCache.init(communicationConfigs);
        LogUtil.info(logger, "initialize http client cache over");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void refresh(Object... obj) {
        LogUtil.info(logger, "begin to initialize http client cache");
        List<CommunicationConfig> config = (List<CommunicationConfig>) obj[0];
        httpClientCache.refresh(config);
        LogUtil.info(logger, "initialize http client cache over ");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void refreshById(String id, Object... obj) {
        LogUtil.info(logger, "begin to refresh  http client cache，id=" + id);
        List<CommunicationConfig> config = (List<CommunicationConfig>) obj[0];
        if (null == config) {
            LogUtil.info(logger, "CommunicationConfig is empty，id=" + id);
            return;
        }
        httpClientCache.refresh(config);
        LogUtil.info(logger, "refresh  http client cache over,id=" + id);
    }

    @Override
    public boolean isInitOk() {
        return httpClientCache.isInitOk();
    }

    /**
     * Setter method for property <tt>httpClientHandler</tt>.
     */
    public void setHttpClientHandler(HttpClientHandler httpClientHandler) {
        this.httpClientHandler = httpClientHandler;
    }

    /**
     * Setter method for property <tt>httpClientCache</tt>.
     */
    public void setHttpClientCache(HttpClientCache httpClientCache) {
        this.httpClientCache = httpClientCache;
    }

    /**
     * @see InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 注册HTTP/HTTPS服务监听器
        CommunicationServiceImpl.registerHandler(TransportProtocol.HTTP, this);
        CommunicationServiceImpl.registerHandler(TransportProtocol.HTTPS, this);
        LogUtil.info(logger, "initialize httpclient cache finish.");
    }

    /**
     * Setter method for property <tt>instproxyHandler</tt>.
     *

     */
    //    public void setInstproxyHandler(InstproxyHandler instproxyHandler) {
    //        this.instproxyHandler = instproxyHandler;
    //    }
}
