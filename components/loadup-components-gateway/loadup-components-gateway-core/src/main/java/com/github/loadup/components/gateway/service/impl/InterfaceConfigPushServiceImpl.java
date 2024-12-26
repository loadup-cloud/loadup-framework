package com.github.loadup.components.gateway.service.impl;

import com.github.loadup.components.gateway.common.util.InterfaceConfigUtil;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.enums.InterfaceStatus;
import com.github.loadup.components.gateway.core.common.enums.InterfaceType;
import com.github.loadup.components.gateway.facade.api.InterfaceConfigPushService;
import com.github.loadup.components.gateway.facade.model.InterfaceDto;
import com.github.loadup.components.gateway.facade.request.APIConfigRequest;
import com.github.loadup.components.gateway.facade.request.SPIConfigRequest;
import com.github.loadup.components.gateway.facade.response.APIConfigResponse;
import com.github.loadup.components.gateway.facade.response.SPIConfigResponse;
import com.github.loadup.components.gateway.repository.RepositoryManager;
import com.github.loadup.components.gateway.service.template.ServiceCallback;
import com.github.loadup.components.gateway.service.template.ServiceTemplate;
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
        ServiceTemplate.execute(request, response, new ServiceCallback() {
            @Override
            public void checkParameter() {
            }

            @Override
            public void preProcess() {
                // 1. build open api url
                buildOpenApiUrl(request);

                // 2. set PLUGIN OPENAPI flag
                if (MapUtils.isEmpty(request.getCommunicationProperties())) {
                    request.setCommunicationProperties(new HashMap<>());
                }

                request.getCommunicationProperties().putIfAbsent(Constant.INTERFACE_TYPE,
                        Constant.PLUGIN_OPENAPI);
            }

            @Override
            public void process() {
                InterfaceDto interfaceDto = buildApiInterfaceDto(request);
                repositoryManager.saveOrUpdateInterface(interfaceDto);
            }
        });

        return response;
    }

    /**
     * @see InterfaceConfigPushService#pushSPIConfig(SPIConfigRequest)
     */
    @Override
    public SPIConfigResponse pushSPIConfig(SPIConfigRequest request) {

        SPIConfigResponse response = new SPIConfigResponse();
        ServiceTemplate.execute(request, response, new ServiceCallback() {
            @Override
            public void checkParameter() {
            }

            @Override
            public void preProcess() {
                // 1. init communication properties if necessary
                if (MapUtils.isEmpty(request.getCommunicationProperties())) {
                    request.setCommunicationProperties(new HashMap<>());
                }
            }

            @Override
            public void process() {

                InterfaceDto interfaceDto = buildSpiInterfaceDto(request);
                repositoryManager.saveOrUpdateInterface(interfaceDto);
            }
        });

        return response;
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