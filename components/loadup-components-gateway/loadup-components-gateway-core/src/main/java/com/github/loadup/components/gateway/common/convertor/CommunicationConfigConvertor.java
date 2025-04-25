package com.github.loadup.components.gateway.common.convertor;

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

import com.github.loadup.components.gateway.certification.util.CommonUtil;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.enums.InterfaceType;
import com.github.loadup.components.gateway.core.common.enums.MessageFormat;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.model.communication.ProtocolType;
import com.github.loadup.components.gateway.core.model.communication.TransportURI;
import com.github.loadup.components.gateway.core.service.InterfaceProdCenterQueryService;
import com.github.loadup.components.gateway.facade.config.model.APIConditionGroup;
import com.github.loadup.components.gateway.facade.config.model.CommunicationPropertiesGroup;
import com.github.loadup.components.gateway.facade.config.model.SPIConditionGroup;
import com.github.loadup.components.gateway.facade.model.CommunicationConfigDto;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.loadup.components.gateway.core.common.Constant.*;

/**
 *
 */
@Component
public class CommunicationConfigConvertor {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(CommunicationConfigConvertor.class);
    /**
     * Communication Properties product center query service
     */
    private static InterfaceProdCenterQueryService interfaceProdCenterQueryService;

    /**
     * convert dto to domain model
     */
    public static CommunicationConfig dto2Model(CommunicationConfigDto item) {
        CommunicationConfig model = new CommunicationConfig();
        try {
            model.setInterfaceId(item.getInterfaceId());
            model.setCommunicationId(item.getCommunicationId());
            model.setProtocol(item.getProtocol());
            model.setRecvMessageFormat(MessageFormat.TEXT);
            model.setProperties(item.getProperties());
            model.setUrl(item.getUri());
            model.setUri(new TransportURI(getURLPath(item.getUri())));
            if (StringUtils.isNotBlank(item.getProperties())) {
                Map<String, String> propertyMap = CommonUtil.Str2Kv(item.getProperties());
                model.setConnectTimeout(StringUtils.isBlank(propertyMap.get(CONNECTION_TIMEOUT))
                        ? DEFAULT_CONNECTION_TIMEOUT
                        : Integer.valueOf(propertyMap.get(CONNECTION_TIMEOUT)));
                model.setReadTimeout(StringUtils.isBlank(propertyMap.get(Constant.READ_TIMEOUT))
                        ? Constant.DEFAULT_READ_TIMEOUT
                        : Integer.valueOf(propertyMap.get(Constant.READ_TIMEOUT)));
                model.setProperties(item.getProperties());
            }
        } catch (Exception e) {
            //log error
            LogUtil.error(logger, e,
                    "failed building communication config for interfaceId=" + item.getInterfaceId());
        }
        return model;
    }

    /**
     * get protocol from url. if no protocol in url, return default HTTP protocol
     * <p>
     * integration_uri follow the pattern of
     * http://domaneName/path
     * https://domainName/path
     * TR://domainName:12000/class/method
     * SPRINGSPRINGBEAN://class/method
     * <p>
     * http://wf.rate.query would be splited into [http, wf.rate.query]
     * http://wf.com/rate/query.htm would be splited into [http, wf.com, rate, query.htm]
     */
    protected static String getProtocol(String url) {
        int index = StringUtils.indexOf(url, URI_SEPARATOR);
        if (index >= 0 && index < (url.length() - 3)) {
            return StringUtils.substring(url, 0, index);
        } else {
            return ProtocolType.HTTP.getCode();
        }
    }

    /**
     * convert APIConditionGroup item model to sender domain model
     */
    public static CommunicationConfig convertToSenderConfig(APIConditionGroup item) {
        if (item == null) {
            return null;
        }
        CommunicationConfig interfaceConfig = new CommunicationConfig();
        interfaceConfig.setInterfaceId(item.getUrl());
        interfaceConfig.setCommunicationId(item.getUrl());
        interfaceConfig.setUrl(item.getUrl());
        interfaceConfig.setUri(new TransportURI(getURLPath(item.getUrl())));
        String protocol = getProtocol(item.getUrl());
        interfaceConfig.setProtocol(protocol);
        interfaceConfig.setRecvMessageFormat(MessageFormat.TEXT);
        setTimeoutToCommunication(InterfaceType.OPENAPI.getCode(), interfaceConfig, item.getCommunicationProperties());
        return interfaceConfig;
    }

    /**
     * convert APIConditionGroup item model to receiver domain model
     */
    public static CommunicationConfig convertToReceiverConfig(APIConditionGroup item) {
        if (item == null) {
            return null;
        }
        CommunicationConfig interfaceConfig = new CommunicationConfig();
        interfaceConfig.setInterfaceId(item.getIntegrationUrl());
        interfaceConfig.setCommunicationId(item.getIntegrationUrl());
        interfaceConfig.setUrl(item.getIntegrationUrl());
        interfaceConfig.setUri(new TransportURI(getURLPath(item.getIntegrationUrl())));
        String protocol = getProtocol(item.getIntegrationUrl());
        interfaceConfig.setProtocol(protocol);
        interfaceConfig.setRecvMessageFormat(MessageFormat.TEXT);
        interfaceConfig.setProperties(item.getCommunicationProperties());
        interfaceConfig.setErrorCodeMap(item.getHttpStatusToErrorCode());
        setTimeoutToCommunication(InterfaceType.OPENAPI.getCode(), interfaceConfig, item.getCommunicationProperties());
        return interfaceConfig;
    }

    /**
     * convert APIConditionGroup item model to receiver domain model
     */
    public static CommunicationConfig convertToReceiverConfig(SPIConditionGroup item) {
        if (item == null) {
            return null;
        }
        CommunicationConfig interfaceConfig = new CommunicationConfig();
        interfaceConfig.setInterfaceId(item.getIntegrationUrl());
        interfaceConfig.setCommunicationId(item.getIntegrationUrl());
        interfaceConfig.setUrl(item.getIntegrationUrl());
        interfaceConfig.setUri(new TransportURI(getURLPath(item.getIntegrationUrl())));
        String protocol = getProtocol(item.getIntegrationUrl());
        interfaceConfig.setProtocol(protocol);
        interfaceConfig.setRecvMessageFormat(MessageFormat.TEXT);
        interfaceConfig.setProperties(item.getCommunicationProperties());
        interfaceConfig.setErrorCodeMap(item.getHttpStatusToErrorCode());
        setTimeoutToCommunication(InterfaceType.SPI.getCode(), interfaceConfig, item.getCommunicationProperties());
        return interfaceConfig;
    }

    /**
     * convert dto list to domain model list
     */
    public static List<CommunicationConfig> dtoList2ModelList(List<CommunicationConfigDto> communicationConfigDtos) {
        List<CommunicationConfig> models = new ArrayList<>();
        if (CollectionUtils.isEmpty(communicationConfigDtos)) {
            return models;
        }
        for (CommunicationConfigDto dto : communicationConfigDtos) {
            models.add(dto2Model(dto));
        }
        return models;
    }

    /**
     * get URL path from uri string
     */
    private static String getURLPath(String url) {
        if (StringUtils.contains(url, URI_SEPARATOR)) {
            return url;
        } else {
            return ProtocolType.HTTP.getCode().concat(URI_SEPARATOR).concat(url);
        }
    }

    /**
     * set timeout communication config
     */
    public static void setTimeoutToCommunication(String transactionType, CommunicationConfig interfaceConfig, String item) {
        if (StringUtils.isNotBlank(item)) {
            Map<String, String> propertyMap = CommonUtil.Str2Kv(item);
            interfaceConfig.setReadTimeout(StringUtils.isBlank(propertyMap.get(Constant.READ_TIMEOUT))
                    ? Constant.DEFAULT_READ_TIMEOUT
                    : Integer.valueOf(propertyMap.get(Constant.READ_TIMEOUT)));
            interfaceConfig.setProperties(item);
        }
        List<String> urls = new ArrayList<>();
        urls.add(interfaceConfig.getInterfaceId());
        urls.add(transactionType);
        urls.add(Constant.TENANT_COMMUNICATION_PROPERTIES_GROUP_URL);
        for (String url : urls) {
            CommunicationPropertiesGroup communicationPropertiesGroup = interfaceProdCenterQueryService.
                    queryCommunicationPropertiesGroup(url);
            if (communicationPropertiesGroup != null && communicationPropertiesGroup.getConnectTimeout() != null
                    && communicationPropertiesGroup.getReadTimeout() != null
            ) {
                interfaceConfig.setReadTimeout(communicationPropertiesGroup.getReadTimeout());
                interfaceConfig.setConnectTimeout(communicationPropertiesGroup.getConnectTimeout());
                break;
            }
        }
    }

    /**
     *
     */
    @Resource
    public void setInterfaceProdCenterQueryService(InterfaceProdCenterQueryService interfaceProdCenterQueryService) {
        CommunicationConfigConvertor.interfaceProdCenterQueryService = interfaceProdCenterQueryService;
    }
}
