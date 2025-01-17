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

import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.components.gateway.facade.model.ClientConfigDto;
import com.github.loadup.components.gateway.facade.model.ClientInterfaceConfigDto;
import com.github.loadup.components.gateway.facade.request.ClientConfigAddRequest;
import com.github.loadup.components.gateway.facade.request.ClientConfigAuthorizeRequest;
import com.github.loadup.components.gateway.facade.request.ClientConfigDeauthorizeRequest;
import com.github.loadup.components.gateway.facade.request.ClientConfigUpdateRequest;

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
