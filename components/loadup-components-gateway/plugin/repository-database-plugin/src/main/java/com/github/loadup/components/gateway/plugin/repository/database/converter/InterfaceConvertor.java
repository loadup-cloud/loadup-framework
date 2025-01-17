package com.github.loadup.components.gateway.plugin.repository.database.converter;

/*-
 * #%L
 * repository-database-plugin
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
