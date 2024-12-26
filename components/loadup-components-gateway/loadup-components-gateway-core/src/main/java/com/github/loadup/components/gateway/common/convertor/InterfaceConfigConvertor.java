package com.github.loadup.components.gateway.common.convertor;

import com.github.loadup.components.gateway.common.util.InterfaceConfigUtil;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.model.InterfaceConfig;
import com.github.loadup.components.gateway.facade.config.model.APIConditionGroup;
import com.github.loadup.components.gateway.facade.config.model.SPIConditionGroup;
import com.github.loadup.components.gateway.facade.enums.InterfaceStatus;
import com.github.loadup.components.gateway.facade.enums.MessageFormat;
import com.github.loadup.components.gateway.facade.model.InterfaceConfigDto;
import com.github.loadup.components.gateway.facade.model.InterfaceDto;
import com.github.loadup.components.gateway.facade.request.*;
import com.github.loadup.components.gateway.facade.response.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 *
 */
public class InterfaceConfigConvertor {

    /**
     * convert dto model to domain model
     */
    public static InterfaceConfig dto2Model(InterfaceConfigDto item) {
        InterfaceConfig interfaceConfig = new InterfaceConfig();
        interfaceConfig.setInterfaceId(item.getInterfaceId());
        interfaceConfig.setInterfaceName(item.getInterfaceName());
        interfaceConfig.setMessageProcessorId(item.getMessageProcessId());
        interfaceConfig.setMessageReceiverInterfaceId(item.getMessageReceiverInterfaceId());
        // 需要考虑如何构建,包括默认版本
        interfaceConfig.setVersion(
                StringUtils.isBlank(item.getVersion()) ? StringUtils.EMPTY : item.getVersion());
        interfaceConfig.setSecurityStrategyCode(item.getCertCode());
        interfaceConfig.setEnable(Boolean.parseBoolean(item.getIsEnable()));
        interfaceConfig.setProperties(item.getProperties());
        return interfaceConfig;
    }

    /**
     * convert APIConditionGroup item model to sender domain model
     */
    public static InterfaceConfig convertToSenderConfig(APIConditionGroup item) {
        if (item == null) {
            return null;
        }
        InterfaceConfig interfaceConfig = new InterfaceConfig();
        interfaceConfig.setInterfaceId(item.getUrl());
        interfaceConfig.setInterfaceName(item.getUrl());
        interfaceConfig.setMessageProcessorId(item.getUrl());
        interfaceConfig.setMessageReceiverInterfaceId(item.getIntegrationUrl());
        interfaceConfig.setVersion(Constant.INTERFACE_DEFAULT_VERSION);
        interfaceConfig.setSecurityStrategyCode(item.getSecurityStrategyCode());
        interfaceConfig.setEnable(true);
        Map<String, String> prop = new HashMap<>();
        if (item.getLimitConn() != null) {
            prop.put(Constant.INTERFACE_LIMIT_KEY, item.getLimitConn().toString());
        }
        interfaceConfig.setPropertiesByMap(prop);
        return interfaceConfig;
    }

    /**
     * convert APIConditionGroup item model to receiver domain model
     */
    public static InterfaceConfig convertToReceiverConfig(APIConditionGroup item) {
        if (item == null) {
            return null;
        }
        InterfaceConfig interfaceConfig = new InterfaceConfig();
        interfaceConfig.setInterfaceId(item.getIntegrationUrl());
        interfaceConfig.setInterfaceName(item.getIntegrationUrl());
        interfaceConfig.setMessageProcessorId(item.getIntegrationUrl());
        interfaceConfig.setMessageReceiverInterfaceId(item.getIntegrationUrl());
        interfaceConfig.setVersion(Constant.INTERFACE_DEFAULT_VERSION);
        interfaceConfig.setSecurityStrategyCode(item.getSecurityStrategyCode());
        interfaceConfig.setEnable(true);
        Map<String, String> prop = new HashMap<>();
        if (item.getLimitConn() != null) {
            prop.put(Constant.INTERFACE_LIMIT_KEY, item.getLimitConn().toString());
        }
        interfaceConfig.setPropertiesByMap(prop);
        return interfaceConfig;
    }

    /**
     * convert SPIConditionGroup item model to receiver domain model
     */
    public static InterfaceConfig convertToReceiverConfig(SPIConditionGroup item) {
        if (item == null) {
            return null;
        }
        InterfaceConfig interfaceConfig = new InterfaceConfig();
        interfaceConfig.setInterfaceId(item.getIntegrationUrl());
        interfaceConfig.setInterfaceName(item.getIntegrationUrl());
        interfaceConfig.setMessageProcessorId(item.getIntegrationUrl());
        interfaceConfig.setMessageReceiverInterfaceId(item.getIntegrationUrl());
        interfaceConfig.setVersion(Constant.INTERFACE_DEFAULT_VERSION);
        interfaceConfig.setSecurityStrategyCode(item.getSecurityStrategyCode());
        interfaceConfig.setEnable(true);
        Map<String, String> prop = new HashMap<>();
        if (item.getLimitConn() != null) {
            prop.put(Constant.INTERFACE_LIMIT_KEY, item.getLimitConn().toString());
        }
        interfaceConfig.setPropertiesByMap(prop);
        return interfaceConfig;
    }

    /**
     * convert dto models to domain models
     */
    public static List<InterfaceConfig> dtoList2ModelList(List<InterfaceConfigDto> interfaceConfigDtos) {
        List<InterfaceConfig> models = new ArrayList<>();
        if (CollectionUtils.isEmpty(interfaceConfigDtos)) {
            return models;
        }
        for (InterfaceConfigDto dto : interfaceConfigDtos) {
            models.add(dto2Model(dto));
        }
        return models;
    }

    /**
     * convert to dto
     */
    public static InterfaceDto convertToDto(InterfaceConfigAddRequest request) {
        if (request == null) {
            return null;
        }
        InterfaceDto dto = new InterfaceDto();
        dto.setCommunicationProperties(request.getCommunicationConfig().getProperties());
        dto.setTenantId(request.getTenantId());
        dto.setIntegrationRequestBodyAssemble(
                request.getProcessConfig().getReceiverRequestBodyAssembleTemplate());
        dto.setIntegrationRequestHeaderAssemble(
                request.getProcessConfig().getReceiverRequestHeaderAssembleTemplate());
        dto.setIntegrationResponseParser(
                request.getProcessConfig().getReceiverResponseParserTemplate());
        dto.setInterfaceRequestParser(request.getProcessConfig().getSenderRequestParserTemplate());
        dto.setInterfaceResponseBodyAssemble(
                request.getProcessConfig().getSenderResponseBodyAssembleTemplate());
        dto.setInterfaceResponseHeaderAssemble(
                request.getProcessConfig().getSenderResponseHeaderAssembleTemplate());
        dto.setInterfaceName(request.getInterfaceName());
        dto.setUrl(request.getCommunicationConfig().getUri());
        dto.setIntegrationUrl(request.getCommunicationConfig().getIntegrationUri());
        dto.setSecurityStrategyCode(request.getSecurityStrategyCode());
        dto.setVersion(request.getVersion());
        dto.setType(request.getInterfaceType().getCode());
        dto.setStatus(InterfaceStatus.INVALID.getCode());
        String interfaceId = InterfaceConfigUtil.generateInterfaceId(
                request.getCommunicationConfig().getUri(), request.getTenantId(), dto.getVersion(),
                request.getInterfaceType().getCode(), request.getCommunicationConfig().getProperties());
        dto.setInterfaceId(interfaceId);
        return dto;
    }

    /**
     * convert to dto
     */
    public static InterfaceDto convertToDto(InterfaceConfigUpdateRequest request) {
        if (request == null) {
            return null;
        }
        InterfaceDto dto = new InterfaceDto();
        dto.setCommunicationProperties(request.getCommunicationConfig().getProperties());
        dto.setTenantId(request.getTenantId());
        dto.setIntegrationRequestBodyAssemble(
                request.getProcessConfig().getReceiverRequestBodyAssembleTemplate());
        dto.setIntegrationRequestHeaderAssemble(
                request.getProcessConfig().getReceiverRequestHeaderAssembleTemplate());
        dto.setIntegrationResponseParser(
                request.getProcessConfig().getReceiverResponseParserTemplate());
        dto.setInterfaceRequestParser(request.getProcessConfig().getSenderRequestParserTemplate());
        dto.setInterfaceResponseBodyAssemble(
                request.getProcessConfig().getSenderResponseBodyAssembleTemplate());
        dto.setInterfaceResponseHeaderAssemble(
                request.getProcessConfig().getSenderResponseHeaderAssembleTemplate());
        dto.setInterfaceName(request.getInterfaceName());
        dto.setUrl(request.getCommunicationConfig().getUri());
        dto.setIntegrationUrl(request.getCommunicationConfig().getIntegrationUri());
        dto.setSecurityStrategyCode(request.getSecurityStrategyCode());
        dto.setVersion(request.getVersion());
        dto.setType(request.getInterfaceType().getCode());
        dto.setStatus(InterfaceStatus.INVALID.getCode());
        dto.setInterfaceId(request.getInterfaceId());
        return dto;
    }

    /**
     * convert to dto
     */
    public static InterfaceDto convertToDto(InterfaceConfigUpgradeRequest request) {
        if (request == null) {
            return null;
        }
        InterfaceDto dto = new InterfaceDto();
        dto.setCommunicationProperties(request.getCommunicationConfig().getProperties());
        dto.setTenantId(request.getTenantId());
        dto.setIntegrationRequestBodyAssemble(
                request.getProcessConfig().getReceiverRequestBodyAssembleTemplate());
        dto.setIntegrationRequestHeaderAssemble(
                request.getProcessConfig().getReceiverRequestHeaderAssembleTemplate());
        dto.setIntegrationResponseParser(
                request.getProcessConfig().getReceiverResponseParserTemplate());
        dto.setInterfaceRequestParser(request.getProcessConfig().getSenderRequestParserTemplate());
        dto.setInterfaceResponseBodyAssemble(
                request.getProcessConfig().getSenderResponseBodyAssembleTemplate());
        dto.setInterfaceResponseHeaderAssemble(
                request.getProcessConfig().getSenderResponseHeaderAssembleTemplate());

        dto.setInterfaceName(request.getInterfaceName());
        dto.setUrl(request.getCommunicationConfig().getUri());
        dto.setIntegrationUrl(request.getCommunicationConfig().getIntegrationUri());
        dto.setSecurityStrategyCode(request.getSecurityStrategyCode());
        dto.setVersion(request.getVersion());
        dto.setType(request.getInterfaceType().getCode());
        dto.setStatus(InterfaceStatus.INVALID.getCode());
        dto.setInterfaceId(request.getInterfaceId());
        return dto;
    }

    /**
     * build interface ConfigList
     */
    public static List<InterfaceConfigInnerResponse> buildinterfaceConfigList(List<InterfaceDto> dtoList) {
        List<InterfaceConfigInnerResponse> responses = new ArrayList<>();
        for (InterfaceDto dto : dtoList) {
            InterfaceConfigInnerResponse innerResponse = new InterfaceConfigInnerResponse();
            innerResponse.setInterfaceId(dto.getInterfaceId());
            innerResponse.setInterfaceName(dto.getInterfaceName());
            innerResponse.setStatus(InterfaceStatus.getByName(dto.getStatus()));
            innerResponse.setVersion(dto.getVersion());

            // build communication config
            CommunicationConfigResponse communicationConfigResponse = new CommunicationConfigResponse();
            communicationConfigResponse.setUri(dto.getUrl());
            Map<String, String> map = dto.getCommunicationProperties();
            communicationConfigResponse.setConnectTimeout(map.get("connectTimeout"));
            communicationConfigResponse.setReadTimeout(map.get("readTimeout"));
            communicationConfigResponse.setUri(map.get("url"));
            communicationConfigResponse.setProperties(map.get("properties"));
            communicationConfigResponse
                    .setRecvMessageFormat(MessageFormat.getEnumByCode(map.get("recvMessageFormat")));
            communicationConfigResponse
                    .setSendMessageFormat(MessageFormat.getEnumByCode(map.get("rendMessageFormat")));
            innerResponse.setCommunicationConfig(communicationConfigResponse);

            // build process config
            MessageProcessConfigResponse processConfigResponse = new MessageProcessConfigResponse();
            processConfigResponse.setSenderRequestParserTemplate(dto.getInterfaceRequestParser());
            processConfigResponse
                    .setSenderResponseBodyAssembleTemplate(dto.getInterfaceResponseBodyAssemble());
            processConfigResponse
                    .setSenderResponseHeaderAssembleTemplate(dto.getInterfaceResponseHeaderAssemble());
            processConfigResponse
                    .setReceiverRequestBodyAssembleTemplate(dto.getIntegrationRequestBodyAssemble());
            processConfigResponse.setReceiverRequestHeaderAssembleTemplate(
                    dto.getIntegrationRequestHeaderAssemble());
            processConfigResponse
                    .setReceiverResponseParserTemplate(dto.getIntegrationResponseParser());
            innerResponse.setProcessConfig(processConfigResponse);

            responses.add(innerResponse);
        }
        return responses;
    }
}