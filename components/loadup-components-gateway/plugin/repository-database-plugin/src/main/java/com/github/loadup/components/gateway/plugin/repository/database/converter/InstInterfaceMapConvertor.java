package com.github.loadup.components.gateway.plugin.repository.database.converter;

import com.github.loadup.components.gateway.facade.model.ClientInterfaceConfigDto;
import com.github.loadup.components.gateway.plugin.repository.database.dal.dataobject.InstInterfaceMapDO;

import java.util.Date;

/**
 *
 */
public class InstInterfaceMapConvertor {
    /**
     * ClientInterfaceConfigDto to InstInterfaceMapDO
     */
    public static InstInterfaceMapDO Dto2DO(ClientInterfaceConfigDto dto) {
        InstInterfaceMapDO instInterfaceMapDO = new InstInterfaceMapDO();
        instInterfaceMapDO.setClientId(dto.getClientId());
        instInterfaceMapDO.setInterfaceId(dto.getInterfaceId());
        instInterfaceMapDO.setGmtCreate(new Date());
        instInterfaceMapDO.setGmtModified(new Date());
        return instInterfaceMapDO;
    }
}