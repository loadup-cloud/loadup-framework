package com.github.loadup.components.gateway.plugin.repository.database.converter;

import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.components.gateway.facade.model.InterfaceDto;
import com.github.loadup.components.gateway.plugin.repository.database.dal.dataobject.InterfaceDO;

import java.util.Date;

/**
 *
 */
public class InterfaceConvertor {

    /**
     * convert interfaceDTO to interfaceDO
     */
    public static InterfaceDO dto2DO(InterfaceDto interfaceDto) {
        InterfaceDO interfaceDO = new InterfaceDO();
        interfaceDO.setTenantId(interfaceDto.getTenantId());
        interfaceDO.setInterfaceId(interfaceDto.getInterfaceId());
        interfaceDO.setInterfaceName(interfaceDto.getInterfaceName());
        interfaceDO.setUrl(interfaceDto.getUrl());
        interfaceDO.setIntegrationUrl(interfaceDto.getIntegrationUrl());
        interfaceDO.setSecurityStrategyCode(interfaceDto.getSecurityStrategyCode());
        interfaceDO.setVersion(interfaceDto.getVersion());
        interfaceDO.setType(interfaceDto.getType());
        interfaceDO.setStatus(interfaceDto.getStatus());
        interfaceDO.setInterfaceRequestParser(interfaceDto.getInterfaceRequestParser());
        interfaceDO.setIntegrationRequestHeaderAssemble(
                interfaceDto.getIntegrationRequestHeaderAssemble());
        interfaceDO
                .setIntegrationRequestBodyAssemble(interfaceDto.getIntegrationRequestBodyAssemble());
        interfaceDO.setIntegrationResponseParser(interfaceDto.getIntegrationResponseParser());
        interfaceDO
                .setInterfaceResponseHeaderAssemble(interfaceDto.getInterfaceResponseHeaderAssemble());
        interfaceDO
                .setInterfaceResponseBodyAssemble(interfaceDto.getInterfaceResponseBodyAssemble());
        interfaceDO.setCommunicationProperties(
                JsonUtil.toJSONString(interfaceDto.getCommunicationProperties()));
        interfaceDO.setGmtCreate(new Date());
        interfaceDO.setGmtModified(new Date());
        return interfaceDO;
    }

    /**
     * convert do object to dto
     */
    public static InterfaceDto do2Dto(InterfaceDO interfaceDO) {
        InterfaceDto interfaceManageDto = new InterfaceDto();
        interfaceManageDto.setTenantId(interfaceDO.getTenantId());
        interfaceManageDto.setCommunicationProperties(
                JsonUtil.jsonToMap(interfaceDO.getCommunicationProperties()));
        interfaceManageDto
                .setIntegrationRequestBodyAssemble(interfaceDO.getIntegrationRequestBodyAssemble());
        interfaceManageDto
                .setIntegrationRequestHeaderAssemble(interfaceDO.getIntegrationRequestHeaderAssemble());
        interfaceManageDto.setIntegrationResponseParser(interfaceDO.getIntegrationResponseParser());
        interfaceManageDto.setInterfaceRequestParser(interfaceDO.getInterfaceRequestParser());
        interfaceManageDto
                .setInterfaceResponseBodyAssemble(interfaceDO.getInterfaceResponseBodyAssemble());
        interfaceManageDto
                .setInterfaceResponseHeaderAssemble(interfaceDO.getInterfaceResponseHeaderAssemble());
        interfaceManageDto.setInterfaceName(interfaceDO.getInterfaceName());
        interfaceManageDto.setUrl(interfaceDO.getUrl());
        interfaceManageDto.setIntegrationUrl(interfaceDO.getIntegrationUrl());
        interfaceManageDto.setSecurityStrategyCode(interfaceDO.getSecurityStrategyCode());
        interfaceManageDto.setVersion(interfaceDO.getVersion());
        interfaceManageDto.setType(interfaceDO.getType());
        interfaceManageDto.setStatus(interfaceDO.getStatus());
        interfaceManageDto.setInterfaceId(interfaceDO.getInterfaceId());
        return interfaceManageDto;
    }

}