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
