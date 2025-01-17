package com.github.loadup.components.gateway.core.communication.http.cache;

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

import com.github.loadup.components.gateway.core.communication.http.impl.HttpClientServiceImpl;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.model.communication.TransportProtocol;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * http客户端缓存
 */
@Component
public class HttpClientCache {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(HttpClientServiceImpl.class);

    /**
     * http客户端缓存map
     */
    private static Map<String, CloseableHttpClient> map = new ConcurrentHashMap<>();

    /**
     * 通讯缓存配置
     */
    private static Map<String, CommunicationConfig> communicationConfigMap = new ConcurrentHashMap<String, CommunicationConfig>();

    /**
     * 是否初始化完成
     */
    private static boolean isInitOk = false;

    /**
     * ssl工具类
     */
    //    private static SSLProtocolHelper sSLProtocolHelper;

    /**
     * 组装客户端
     */
    private static CloseableHttpClient constructClient(CommunicationConfig config) {

        //注册https协议
        registerSSLProtocol(config);
        CloseableHttpClient client = HttpClients.createDefault();
        //        HttpConnectionManager httpConnectionManager = new CustomizeHttpConnectionManager(
        //                HttpConfigUtil.getIdelTimeout(config));
        //        HttpConnectionManagerParams connectionManagerParams = new HttpConnectionManagerParams();
        //
        //        // 连接超时时间
        //        connectionManagerParams.setConnectionTimeout(config.getConnectTimeout());
        //
        //        // 等待数据最大时间
        //        connectionManagerParams.setSoTimeout(config.getReadTimeout());
        //
        //        int limiter = CommunicationConfigUtil.getConnectionPoolSize(config);
        //        int limiterPerHost = CommunicationConfigUtil.getConnectionSizePerHost(config);
        //        connectionManagerParams.setDefaultMaxConnectionsPerHost(limiterPerHost);
        //        connectionManagerParams.setMaxTotalConnections(limiter);
        //
        //        httpConnectionManager.setParams(connectionManagerParams);
        //        HttpClient client = new HttpClient(httpConnectionManager);
        //        client.getParams().setConnectionManagerTimeout(config.getConnectTimeout());
        //        DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(0, false);
        //        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);
        return client;
    }

    /**
     * 注册SSL协议
     */
    private static void registerSSLProtocol(CommunicationConfig config) {
        //        if (StringUtils.equals(config.getProtocol(), TransportProtocol.HTTPS) || StringUtils.equals(config.getProtocol(),
        //        TransportProtocol.INSTPROXY)) {
        //            TransportURI url = config.getUri();
        //            String scheme = url.getSchema();
        //            if (TransportProtocol.HTTP.equalsIgnoreCase(scheme)) {
        //                //http的url不可以注册https协议
        //                return;
        //            }
        //            scheme = StringUtils.toLowerCase(scheme); //协议scheme必需要小写，否则无法引用
        //            SSLContext context = sSLProtocolHelper.create(config);
        //            SSLSocketFactory socketFactory = context.getSocketFactory();
        //            ProtocolSocketFactory factory = new SSLSocketFactoryImpl(socketFactory, config);
        //
        //            // 注册的协议只能使用全小写
        //            Protocol myhttps = new Protocol(scheme, factory, url.getPort());
        //            Protocol.registerProtocol(scheme, myhttps);
        //
        //        }
    }

    /**
     * 获取客户端
     */
    public CloseableHttpClient getClient(CommunicationConfig communicationConfig) {
        String communicationId = communicationConfig.getCommunicationId();
        String protocol = communicationConfig.getProtocol();
        if (StringUtils.isBlank(communicationId)
                || (StringUtils.equals(TransportProtocol.HTTP, protocol) && StringUtils.equals(TransportProtocol.HTTPS, protocol)
                && StringUtils.equals(TransportProtocol.INSTPROXY, protocol))) {
            return null;
        }
        CloseableHttpClient client = map.get(communicationId);
        if (client == null) {
            synchronized (map) {
                if ((client = map.get(communicationId)) == null) {
                    try {
                        client = constructClient(communicationConfig);
                        map.put(communicationId, client);
                    } catch (Exception ex) {
                        LogUtil.error(logger, ex,
                                "failed init httpclient , current config is ,communicationConfig=" + communicationConfig);
                    }
                }
            }
        }
        return client;
    }

    /**
     * 初始化
     */
    public void init(List<CommunicationConfig> communicationConfigs) {
        if (communicationConfigs != null) {
            for (CommunicationConfig cfg : communicationConfigs) {
                communicationConfigMap.put(cfg.getCommunicationId(), cfg);
            }
        } else {
            LogUtil.warn(logger, "HttpClientCache init parameter is null! CommunicationConfigMap not built!");
        }

        isInitOk = true;
    }

    /**
     * 根据通讯属性id获取通讯配置
     */
    public CommunicationConfig getCommunicationConfigById(String communicationId) {
        return communicationConfigMap.get(communicationId);
    }

    /**
     * 刷新
     */
    public void refresh(List<CommunicationConfig> config) {
        for (CommunicationConfig cc : config) {
            // CommunicationConfigMap缓存不做协议隔离
            communicationConfigMap.put(cc.getCommunicationId(), cc);

            if (!StringUtils.equals(cc.getProtocol(), TransportProtocol.HTTP)
                    && !StringUtils.equals(cc.getProtocol(), TransportProtocol.HTTPS)
                    && !StringUtils.equals(cc.getProtocol(), TransportProtocol.INSTPROXY)) {
                continue;
            }
            try {
                //刷新的时候删除对应key的实例, 因为获取的时候会自动构建
                //不能使用入参的config来构建缓存, 推过来的配置没有appId, 会导致配置https连接证书的httpClient无法拿到对应证书
                map.remove(cc.getCommunicationId());
            } catch (Exception ex) {
                LogUtil.error(logger, ex, "failed init httpclient , ignore current config ,communicationConfig=" + cc);
            }
        }
    }

    /**
     * Getter method for property <tt>isInitOk</tt>.
     */
    public boolean isInitOk() {
        return isInitOk;
    }

    /**
     * Setter method for property <tt>isInitOk</tt>.
     */
    public void setInitOk(boolean isInitOk) {
        this.isInitOk = isInitOk;
    }

    /**
     * Setter method for property <tt>sSLProtocolHelper</tt>.
     *

     */
    //    public void setsSLProtocolHelper(SSLProtocolHelper sSLProtocolHelper) {
    //        this.sSLProtocolHelper = sSLProtocolHelper;
    //    }
}
