package com.github.loadup.components.gateway.core.model;

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

import com.github.loadup.commons.result.ResultCode;
import com.github.loadup.components.gateway.certification.exception.CertificationErrorCode;
import com.github.loadup.components.gateway.common.error.LimitRuleErrorCode;
import com.github.loadup.components.gateway.common.error.OauthErrorCode;
import com.github.loadup.components.gateway.common.util.PropertiesUtil;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.core.common.enums.MessageFormat;
import com.github.loadup.components.gateway.core.common.enums.PropertyName;
import com.github.loadup.components.gateway.core.communication.common.code.CommunicationErrorCode;
import com.github.loadup.components.gateway.core.model.communication.ConnectionType;
import com.github.loadup.components.gateway.core.model.communication.TransportURI;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.message.common.errorr.ParserErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * 通信配置
 */
public class CommunicationConfig {

    private static final Logger logger = LoggerFactory.getLogger(CommunicationConfig.class);

    private static final LinkedHashSet<ResultCode> RESULT_CODES = new LinkedHashSet<>();

    static {
        Collections.addAll(RESULT_CODES, CertificationErrorCode.values());
        Collections.addAll(RESULT_CODES, ParserErrorCode.values());
        Collections.addAll(RESULT_CODES, CommunicationErrorCode.values());
        Collections.addAll(RESULT_CODES, OauthErrorCode.values());
        Collections.addAll(RESULT_CODES, LimitRuleErrorCode.values());
        Collections.addAll(RESULT_CODES, GatewayErrorCode.values());
    }

    /**
     * 通信组件id
     */
    private String communicationId;

    /**
     * 通信协议
     */
    private String protocol;

    /**
     * 来报对应接口id
     */
    private String interfaceId;

    /**
     * Server/Client
     */
    private ConnectionType connectionType;

    /**
     * 请求地址/来报接收地址
     */
    private TransportURI uri;

    /**
     * url info
     */
    private String url;

    /**
     * 发送消息类型
     */
    private MessageFormat recvMessageFormat;

    /**
     * 接收消息类型
     */
    private MessageFormat sendMessageFormat;

    /**
     * 连接超时
     */
    private int connectTimeout = 8000;

    /**
     * 读取超时
     */
    private int readTimeout = 8000;

    /**
     * 应用id，和通信证书相关，由框架注入
     */
    private String appId;

    /**
     * 通信属性参数
     */
    private Properties properties = new Properties();

    /**
     * http status map to error code
     */
    private Map<Integer, ResultCode> errorCodeMap = new HashMap<>();

    /**
     * 从map转换出配置
     */
    public static CommunicationConfig parse(Map<String, String> inputMap) {
        CommunicationConfig communicationConfig = new CommunicationConfig();
        communicationConfig.setCommunicationId(inputMap.get("communication_id"));

        String protocol = inputMap.get("protocol");
        communicationConfig.setProtocol(protocol);

        communicationConfig.setInterfaceId(inputMap.get("interface_id"));

        String connectionType = inputMap.get("connection_type");
        communicationConfig.setConnectionType(ConnectionType.getEnumByCode(connectionType));

        String uri = inputMap.get("uri");
        TransportURI transportURI = new TransportURI(uri);
        communicationConfig.setUri(transportURI);

        String recvMessageFormat = inputMap.get("recv_message_format");
        communicationConfig.setRecvMessageFormat(MessageFormat.getEnumByCode(recvMessageFormat));

        String sendMessageFormat = inputMap.get("send_message_format");
        communicationConfig.setSendMessageFormat(MessageFormat.getEnumByCode(sendMessageFormat));

        String connectTimeout = inputMap.get("connect_timeout");
        communicationConfig.setConnectTimeout(Integer.parseInt(connectTimeout));

        String readTimeout = inputMap.get("read_timeout");
        communicationConfig.setReadTimeout(Integer.parseInt(readTimeout));

        String properties = inputMap.get("properties");
        communicationConfig.setProperties(properties);

        return communicationConfig;
    }

    /**
     * 克隆自身对象
     *
     * @see Object#clone()
     */
    public CommunicationConfig clone() {
        CommunicationConfig dest = new CommunicationConfig();
        dest.properties = this.properties;
        dest.errorCodeMap = this.errorCodeMap;
        cloneCommonAttribute(dest);
        dest.uri = this.uri;
        dest.url = this.url;
        dest.appId = this.appId;
        return dest;
    }

    /**
     * Deep clone communication config.
     */
    public CommunicationConfig deepClone() {
        CommunicationConfig dest = new CommunicationConfig();
        dest.properties = this.properties == null ? null : this.properties.clone();
        dest.errorCodeMap = this.errorCodeMap == null ? null : new HashMap<>(this.errorCodeMap);
        dest.uri = this.uri == null ? null : this.uri.clone();
        cloneCommonAttribute(dest);
        dest.appId = this.appId;
        return dest;
    }

    private void cloneCommonAttribute(CommunicationConfig dest) {
        dest.communicationId = this.communicationId;
        dest.connectionType = this.connectionType;
        dest.connectTimeout = this.connectTimeout;
        dest.interfaceId = this.interfaceId;
        dest.protocol = this.protocol;
        dest.readTimeout = this.readTimeout;
        dest.recvMessageFormat = this.recvMessageFormat;
        dest.sendMessageFormat = this.sendMessageFormat;
    }

    /**
     * 获取属性
     */
    public String getProperty(PropertyName name) {
        return PropertiesUtil.getProperty(properties, name);
    }

    /**
     * Is server boolean.
     */
    public boolean isServer() {
        return connectionType == ConnectionType.SERVER;
    }

    /**
     * 获取接收类型
     */
    public MessageFormat getRecvMessageFormat() {
        return this.recvMessageFormat;
    }

    /**
     * Setter method for property <tt>recvMessageFormat</tt>.
     */
    public void setRecvMessageFormat(MessageFormat recvMessageFormat) {
        this.recvMessageFormat = recvMessageFormat;
    }

    /**
     * 获取发送类型
     */
    public MessageFormat getSendMessageFormat() {
        return this.sendMessageFormat;
    }

    /**
     * Setter method for property <tt>sendMessageFormat</tt>.
     */
    public void setSendMessageFormat(MessageFormat sendMessageFormat) {
        this.sendMessageFormat = sendMessageFormat;
    }

    /**
     * Getter method for property <tt>communicationId</tt>.
     */
    public String getCommunicationId() {
        return communicationId;
    }

    /**
     * Setter method for property <tt>communicationId</tt>.
     */
    public void setCommunicationId(String communicationId) {
        this.communicationId = communicationId;
    }

    /**
     * Getter method for property <tt>uri</tt>.
     */
    public TransportURI getUri() {
        return uri;
    }

    /**
     * Setter method for property <tt>uri</tt>.
     */
    public void setUri(TransportURI uri) {
        this.uri = uri;
    }

    /**
     * Getter method for property <tt>connectTimeout</tt>.
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Setter method for property <tt>connectTimeout</tt>.
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Getter method for property <tt>readTimeout</tt>.
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Setter method for property <tt>readTimeout</tt>.
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Getter method for property <tt>properties</tt>.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Setter method for property <tt>properties</tt>.
     */
    public void setProperties(String properties) {
        try {
            this.properties.setProperties(properties);
        } catch (Exception e) {
            LogUtil.error(logger, e, "Communication Config properties is ");
        }

    }

    /**
     * Getter method for property <tt>errorCodeMap</tt>.
     */
    public Map<Integer, ResultCode> getErrorCodeMap() {
        return errorCodeMap;
    }

    /**
     * Setter method for property <tt>errorCodeMap</tt>.
     */
    public void setErrorCodeMap(Map<Integer, ResultCode> errorCodeMap) {
        this.errorCodeMap = errorCodeMap;
    }

    /**
     * Setter method for property <tt>errorCodeMap</tt>.
     */
    public void setErrorCodeMap(String errorCodeMapString) {
        if (StringUtils.isEmpty(errorCodeMapString)) {
            return;
        }
        Map<Integer, ResultCode> codeMap = new HashMap<>();
        // example: 429=REQUEST_TRAFFIC_EXCEED_LIMIT;404=INTERFACE_NOT_EXIST
        String[] configs = Constant.CONFIG_SEPARATOR.split(errorCodeMapString);
        for (String config : configs) {
            int firstSplitIndex = config.indexOf(Constant.VALUE_SEPARATOR);
            if (-1 == firstSplitIndex) {
                continue;
            }
            Integer keyString = Integer.valueOf(config.substring(0, firstSplitIndex).trim());
            for (ResultCode errorCode : RESULT_CODES) {
                if (StringUtils.equals(errorCode.getCode(),
                        config.substring(firstSplitIndex + 1))) {
                    codeMap.put(keyString, errorCode);
                    break;
                }
            }
        }
        this.errorCodeMap = codeMap;
    }

    /**
     * get http status map to error code
     */
    public ResultCode getHttpStatusErrorCode(int httpStatus) {
        if (this.errorCodeMap.containsKey(httpStatus)) {
            return errorCodeMap.get(httpStatus);
        }
        return null;
    }

    /**
     * Setter method for property <tt>properties</tt>.
     */
    public void setPropertiesByMap(Map<String, String> properties) {
        try {
            this.properties.setProperties(properties);
        } catch (Exception e) {
            LogUtil.error(logger, e, "Communication Config properties is ", properties);
        }

    }

    /**
     * Getter method for property <tt>protocol</tt>.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Setter method for property <tt>protocol</tt>.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Getter method for property <tt>connectionType</tt>.
     */
    public ConnectionType getConnectionType() {
        return connectionType;
    }

    /**
     * Setter method for property <tt>connectionType</tt>.
     */
    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    /**
     * Getter method for property <tt>interfaceId</tt>.
     */
    public String getInterfaceId() {
        return interfaceId;
    }

    /**
     * Setter method for property <tt>interfaceId</tt>.
     */
    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    /**
     * Getter method for property <tt>appId</tt>.
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Setter method for property <tt>appId</tt>.
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * Gets get url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets set url.
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
