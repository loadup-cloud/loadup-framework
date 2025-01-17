package com.github.loadup.components.gateway.service.impl;

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

import com.github.loadup.commons.result.Result;
import com.github.loadup.commons.template.ServiceTemplate;
import com.github.loadup.commons.util.ValidateUtils;
import com.github.loadup.components.gateway.common.util.InterfaceConfigUtil;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.core.common.enums.InterfaceStatus;
import com.github.loadup.components.gateway.core.common.enums.InterfaceType;
import com.github.loadup.components.gateway.facade.api.InterfaceConfigPushService;
import com.github.loadup.components.gateway.facade.model.InterfaceDto;
import com.github.loadup.components.gateway.facade.request.APIConfigRequest;
import com.github.loadup.components.gateway.facade.request.SPIConfigRequest;
import com.github.loadup.components.gateway.facade.response.APIConfigResponse;
import com.github.loadup.components.gateway.facade.response.SPIConfigResponse;
import com.github.loadup.components.gateway.repository.RepositoryManager;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * interface config push service
 */
@Component("gatewayInterfaceConfigPushService")
public class InterfaceConfigPushServiceImpl implements InterfaceConfigPushService {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(InterfaceConfigPushServiceImpl.class);

    /**
     * repository manager
     */
    @Resource
    private RepositoryManager repositoryManager;

    /**
     * gateway domain, default is empty
     */
    @Value("${gateway.domain:}")
    private String gatewaydomain;

    /**
     * @see InterfaceConfigPushService#pushAPIConfig(APIConfigRequest)
     */
    @Override
    public APIConfigResponse pushAPIConfig(APIConfigRequest request) {

        APIConfigResponse response = new APIConfigResponse();
        return ServiceTemplate.execute(
                // check parameter
                (Void) -> ValidateUtils.validate(request),
                // process
                () -> {
                    buildOpenApiUrl(request);

                    // 2. set PLUGIN OPENAPI flag
                    if (MapUtils.isEmpty(request.getCommunicationProperties())) {
                        request.setCommunicationProperties(new HashMap<>());
                    }

                    request.getCommunicationProperties().putIfAbsent(Constant.INTERFACE_TYPE,
                            Constant.PLUGIN_OPENAPI);

                    InterfaceDto interfaceDto = buildApiInterfaceDto(request);
                    repositoryManager.saveOrUpdateInterface(interfaceDto);
                    return response;
                },
                // compose exception response
                (e) -> Result.buildFailure(GatewayErrorCode.UNKNOWN_EXCEPTION),
                // compose digest log
                (Void) -> {
                });
    }

    /**
     * @see InterfaceConfigPushService#pushSPIConfig(SPIConfigRequest)
     */
    @Override
    public SPIConfigResponse pushSPIConfig(SPIConfigRequest request) {

        SPIConfigResponse response = new SPIConfigResponse();

        return ServiceTemplate.execute(
                // check parameter
                (Void) -> ValidateUtils.validate(request),
                // process
                () -> {
                    if (MapUtils.isEmpty(request.getCommunicationProperties())) {
                        request.setCommunicationProperties(new HashMap<>());
                    }
                    InterfaceDto interfaceDto = buildSpiInterfaceDto(request);
                    repositoryManager.saveOrUpdateInterface(interfaceDto);
                    return response;
                },
                // compose exception response
                (e) -> Result.buildFailure(GatewayErrorCode.UNKNOWN_EXCEPTION),
                // compose digest log
                (Void) -> {
                });
    }

    /**
     * build api interface dto
     */
    private InterfaceDto buildApiInterfaceDto(APIConfigRequest apiConfig) {

        InterfaceDto apiInterfaceDto = new InterfaceDto();
        apiInterfaceDto.setTenantId(apiConfig.getTenantId());
        String version = StringUtils.defaultIfBlank(apiConfig.getVersion(),
                Constant.INTERFACE_DEFAULT_VERSION);

        String interfaceId = InterfaceConfigUtil.generateInterfaceId(apiConfig.getIntegrationUrl(),
                apiConfig.getTenantId(), version, InterfaceType.OPENAPI.getCode(),
                apiConfig.getCommunicationProperties());

        apiInterfaceDto.setInterfaceId(interfaceId);
        apiInterfaceDto.setInterfaceName(interfaceId);

        apiInterfaceDto.setUrl(apiConfig.getUrl());
        apiInterfaceDto.setIntegrationUrl(apiConfig.getIntegrationUrl());
        apiInterfaceDto.setSecurityStrategyCode(apiConfig.getSecurityStrategyCode());
        apiInterfaceDto.setVersion(version);
        apiInterfaceDto.setType(InterfaceType.OPENAPI.getCode());
        apiInterfaceDto.setStatus(InterfaceStatus.VALID.getCode());

        String openApiMsgParser = apiConfig.getCommunicationProperties()
                .get(Constant.OPENAPI_MSG_PARSER);
        String msgBodyAssemble = apiConfig.getCommunicationProperties()
                .get(Constant.MES_BODY_ASSEMBLE);
        String msgHeaderAssemble = apiConfig.getCommunicationProperties()
                .get(Constant.MSG_HEADER_ASSEMBLE);

        apiInterfaceDto.setInterfaceRequestParser(openApiMsgParser);
        apiInterfaceDto.setInterfaceResponseHeaderAssemble(msgBodyAssemble);
        apiInterfaceDto.setInterfaceResponseBodyAssemble(msgHeaderAssemble);

        apiInterfaceDto
                .setIntegrationRequestHeaderAssemble(apiConfig.getIntegrationRequestHeaderAssemble());
        apiInterfaceDto
                .setIntegrationRequestBodyAssemble(apiConfig.getIntegrationRequestBodyAssemble());
        apiInterfaceDto.setIntegrationResponseParser(apiConfig.getIntegrationResponseParser());

        apiInterfaceDto.setCommunicationProperties(apiConfig.getCommunicationProperties());

        return apiInterfaceDto;

    }

    /**
     * build Spi interface DTO
     */
    private InterfaceDto buildSpiInterfaceDto(SPIConfigRequest spiConfig) {

        InterfaceDto spiInterfaceDto = new InterfaceDto();
        spiInterfaceDto.setTenantId(spiConfig.getTenantId());
        String version = StringUtils.defaultIfBlank(spiConfig.getVersion(),
                Constant.INTERFACE_DEFAULT_VERSION);

        String interfaceId = InterfaceConfigUtil.generateInterfaceId(spiConfig.getIntegrationUrl(),
                spiConfig.getTenantId(), version, InterfaceType.SPI.getCode(),
                spiConfig.getCommunicationProperties());

        spiInterfaceDto.setInterfaceId(interfaceId);
        spiInterfaceDto.setInterfaceName(interfaceId);

        spiInterfaceDto.setUrl("");
        spiInterfaceDto.setIntegrationUrl(spiConfig.getIntegrationUrl());
        spiInterfaceDto.setSecurityStrategyCode(spiConfig.getSecurityStrategyCode());
        spiInterfaceDto.setVersion(version);
        spiInterfaceDto.setType(InterfaceType.SPI.getCode());
        spiInterfaceDto.setStatus(InterfaceStatus.VALID.getCode());
        spiInterfaceDto.setInterfaceRequestParser("");
        spiInterfaceDto.setInterfaceResponseHeaderAssemble("");
        spiInterfaceDto.setInterfaceResponseBodyAssemble("");

        spiInterfaceDto
                .setIntegrationRequestHeaderAssemble(spiConfig.getIntegrationRequestHeaderAssemble());
        spiInterfaceDto
                .setIntegrationRequestBodyAssemble(spiConfig.getIntegrationRequestBodyAssemble());
        spiInterfaceDto.setIntegrationResponseParser(spiConfig.getIntegrationResponseParser());

        spiInterfaceDto.setCommunicationProperties(spiConfig.getCommunicationProperties());

        return spiInterfaceDto;

    }

    /**
     *
     */
    private void buildOpenApiUrl(APIConfigRequest request) {
        StringBuilder url = new StringBuilder();
        if (StringUtils.isNotBlank(gatewaydomain)) {
            url.append(StringUtils.stripEnd(gatewaydomain, Constant.PATH_SEPARATOR));
        }
        if (StringUtils.isNotBlank(request.getTenantId())) {
            url.append(Constant.PATH_SEPARATOR).append(request.getTenantId());
        }
        url.append(Constant.PATH_SEPARATOR)
                .append(StringUtils.stripStart(request.getUrl(), Constant.PATH_SEPARATOR));

        request.setUrl(url.toString());
    }
}
