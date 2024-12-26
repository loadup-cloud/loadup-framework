package com.github.loadup.components.gateway.common.convertor;

import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.components.gateway.facade.model.ClientConfigDto;
import com.github.loadup.components.gateway.facade.model.ClientInterfaceConfigDto;
import com.github.loadup.components.gateway.facade.request.*;

import java.util.Date;

/**
 *
 */
public class ClientConfigConvertor {
    /**
     * ClientConfigAddRequest to ClientConfigDto
     */
    public static ClientConfigDto addRequest2Dto(ClientConfigAddRequest request) {
        ClientConfigDto dto = new ClientConfigDto();
        if (request == null) {
            return dto;
        }
        dto.setClientId(request.getClientId());
        dto.setName(request.getName());
        if (request.getProperties() != null) {
            dto.setProperties(JsonUtil.toJSONString(request.getProperties()));
        }
        dto.setGmtCreate(new Date());
        dto.setGmtModified(new Date());
        return dto;
    }

    /**
     * ClientConfigAuthorizeRequest to ClientConfigDto
     */
    public static ClientInterfaceConfigDto authorizeRequest2Dto(ClientConfigAuthorizeRequest request) {
        ClientInterfaceConfigDto dto = new ClientInterfaceConfigDto();
        if (request == null) {
            return dto;
        }
        dto.setClientId(request.getClientId());
        dto.setInterfaceId(request.getInterfaceId());
        return dto;
    }

    /**
     * ClientConfigDeauthorizeRequest to ClientConfigDto
     */
    public static ClientInterfaceConfigDto deauthorizeRequest2Dto(ClientConfigDeauthorizeRequest request) {
        ClientInterfaceConfigDto dto = new ClientInterfaceConfigDto();
        if (request == null) {
            return dto;
        }
        dto.setClientId(request.getClientId());
        dto.setInterfaceId(request.getInterfaceId());
        return dto;
    }

    /**
     * ClientConfigUpdateRequest to ClientConfigDto
     */
    public static ClientConfigDto updateRequest2Dto(ClientConfigUpdateRequest request) {
        ClientConfigDto dto = new ClientConfigDto();
        if (request == null) {
            return dto;
        }
        dto.setClientId(request.getClientId());
        dto.setName(request.getName());
        if (request.getProperties() != null) {
            dto.setProperties(JsonUtil.toJSONString(request.getProperties()));
        }
        dto.setGmtModified(new Date());
        return dto;
    }
}