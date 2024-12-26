package com.github.loadup.components.gateway.plugin.repository.database.converter;

import com.github.loadup.components.gateway.facade.model.ClientConfigDto;
import com.github.loadup.components.gateway.plugin.repository.database.dal.dataobject.InstDO;

/**
 *
 */
public class InstConvertor {
    /**
     * ClientConfigDto to InstDO
     */
    public static InstDO dto2DO(ClientConfigDto clientConfigDto) {
        if (clientConfigDto == null) {
            return null;
        }
        InstDO instDO = new InstDO();
        instDO.setClientId(clientConfigDto.getClientId());
        instDO.setName(clientConfigDto.getName());
        instDO.setProperties(clientConfigDto.getProperties());
        instDO.setTenantId(clientConfigDto.getTenantId());
        instDO.setGmtCreate(clientConfigDto.getGmtCreate());
        instDO.setGmtModified(clientConfigDto.getGmtModified());
        return instDO;
    }

    /**
     * InstDO to ClientConfigDto
     */
    public static ClientConfigDto DO2dto(InstDO instDO) {
        if (instDO == null) {
            return null;
        }
        ClientConfigDto dto = new ClientConfigDto();
        dto.setClientId(instDO.getClientId());
        dto.setName(instDO.getName());
        dto.setProperties(instDO.getProperties());
        return dto;
    }
}