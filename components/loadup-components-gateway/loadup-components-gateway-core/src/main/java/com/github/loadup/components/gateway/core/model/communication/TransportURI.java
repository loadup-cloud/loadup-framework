package com.github.loadup.components.gateway.core.model.communication;

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

import com.github.loadup.components.gateway.common.exception.util.AssertUtil;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.core.prototype.util.ExceptionUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.net.InetSocketAddress;

/**
 * <p>URL封装，对URL进行协议、地址、格式的进行合法性校验并进行格式转换</p>
 * <p>example:</p>
 * <p>TransportURL url = new TransportURL("tcp://localhost:8080/hello");</p>
 * <p>transport=tcp, host=localhost, port=8080</p>
 * <p>TransportURL url = new TransportURL("https://localhost:8446,443/hello");</p>
 * <p>transport=http, host=localhost, port=443,url=https://localhost:8446/hello</p>
 */
public class TransportURI {

    /**
     * HTTPS协议 默认端口
     */
    private static final int HTTPS_DEFAULT_PORT = 443;

    /**
     * HTTP协议 默认端口
     */
    private static final int HTTP_DEFAULT_PORT = 80;

    /**
     * URL格式 tcp://localhost:8080/hello
     */
    private String url;

    /**
     * 传输协议
     */
    private String transport;

    /**
     * TCP的地址
     */
    private String host;

    /**
     * TCP端口
     */
    private int port;

    /**
     * 模式
     */
    private String schema;

    /**
     *
     */
    public TransportURI(String url) {

        this(url, null);
    }

    /**
     * 适用于自定义schema的初始化方法
     */
    public TransportURI(String url, String schemaId) {

        AssertUtil.isNotBlank(url, GatewayErrorCode.PARAM_ILLEGAL, "URL不能为空");

        //1. 解析传输协议
        parseTransportURL(url, schemaId);

        //2. 重置检查Host信息
        reCheckHostInfo();
    }

    /**
     * Constructor.
     */
    public TransportURI(TransportURI transportURI) {
        this.url = transportURI.getUrl();
        this.transport = transportURI.getTransport();
        this.host = transportURI.getHost();
        this.schema = transportURI.getSchema();
    }

    /**
     * 重置检查Host信息，处理默认端口
     */
    private void reCheckHostInfo() {

        if (StringUtils.equals(transport, TransportProtocol.TCP)) {
            InetSocketAddress address = new InetSocketAddress(this.host, this.port);

            if (null == address.getAddress()) {

                // 地址不合法时跳过，不抛出异常，防止缓存初始化失败
                return;
            }

            this.host = address.getAddress().getHostAddress();
            this.port = address.getPort();
        } else if (StringUtils.equals(transport, TransportProtocol.HTTP)) {

            this.port = this.port < 0 ? HTTP_DEFAULT_PORT : this.port;
        } else {
            this.port = this.port < 0 ? HTTPS_DEFAULT_PORT : this.port;
        }
    }

    /**
     * 解析协议
     */
    private void parseTransportURL(String uri, String schemaId) {
        try {
            schema = getProtocol(uri);
            this.transport = schema;
            String[] protocolInfo = getProtocolInfo(uri);
            this.host = protocolInfo[0];
            if (protocolInfo.length == 3) {
                int idx = -1;
                //里面含有代理端口
                if ((idx = protocolInfo[1].indexOf(',')) > -1) {
                    this.url = schema + "://" + this.host + ":" + protocolInfo[1].substring(0, idx)
                            + protocolInfo[2];
                    this.port = NumberUtils.toInt(protocolInfo[1].substring(idx + 1), -1);
                } else {
                    this.url = uri;
                    this.port = NumberUtils.toInt(protocolInfo[1], -1);
                }
            }

            int schemaIndex = uri.indexOf("://");
            //HTTPS的情况，重新定义schemaId
            if (StringUtils.equals(transport, TransportProtocol.HTTPS) && !StringUtils.isBlank(schemaId)
                    && schemaIndex != -1) {
                uri = uri.substring(schemaIndex + 3, uri.length());
                this.url = schemaId + "://" + uri;
                this.schema = schemaId;
            }

        } catch (Exception e) {
            ExceptionUtil.caught(e, "解析协议错误.url=[", url, "]");

            throw new IllegalArgumentException("解析协议错误.");
        }

    }

    /**
     * 解析传输协议
     */
    private String getProtocol(String inputUrl) {
        return inputUrl.substring(0, inputUrl.indexOf("://"));
    }

    /**
     * 解析传输协议
     */
    private String[] getProtocolInfo(String inputUrl) {
        int idx1 = inputUrl.indexOf("://");
        String subUrl = inputUrl.substring(idx1 + 3);
        int idx2 = subUrl.indexOf('/');
        String queryPart = "";
        //如果不存在，以最后长度结尾
        if (idx2 < 0) {
            idx2 = subUrl.length();
        } else {
            queryPart = subUrl.substring(idx2);
        }
        String[] result = new String[3];
        String[] domainPart = subUrl.substring(0, idx2).split("\\:");
        result[0] = domainPart[0];
        result[1] = domainPart.length > 1 ? domainPart[1] : "";
        result[2] = queryPart;
        return result;
    }

    /**
     * 
     */
    public String getUrl() {
        return url;
    }

    /**
     * 
     */
    public String getHost() {
        return host;
    }

    /**
     * 
     */
    public int getPort() {
        return port;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder retValue = new StringBuilder("TransportURL[");
        retValue.append("url=").append(this.url).append(',');
        retValue.append("transport=").append(this.transport).append(',');
        retValue.append("host=").append(this.host).append(',');
        retValue.append("port=").append(this.port);
        retValue.append(']');
        return retValue.toString();
    }

    /**
     * 
     */
    public String getSchema() {
        return schema;
    }

    /**
     * 
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * 更换请求url中的 domain即: schema+host+port 部分
     * eg1：
     * http://xxx.xxx.com/car/interface.htm
     * ↓
     * https://abc.def.com/car/interface.htm
     * eg2:
     * http://111.111.111.111:8080/car/interface.htm
     * ↓
     * http://222.222.222.222:9090/car/interface.htm
     */
    public void replaceDomain(String newDomain) {
        int idx1 = url.indexOf("://");
        int idx2 = url.indexOf('/', idx1 + 3);
        url = newDomain + url.substring(idx2);

        //1. 解析传输协议
        parseTransportURL(url, null);

        //2. 重置检查Host信息
        reCheckHostInfo();
    }

    /**
     * 
     */
    public String getTransport() {
        return transport;
    }

    /**
     * 
     */
    public void setTransport(String transport) {
        this.transport = transport;
    }

    /**
     * Clone transport uri.
     */
    public TransportURI clone() {
        TransportURI newTransportURI = new TransportURI(this);
        return newTransportURI;
    }
}
