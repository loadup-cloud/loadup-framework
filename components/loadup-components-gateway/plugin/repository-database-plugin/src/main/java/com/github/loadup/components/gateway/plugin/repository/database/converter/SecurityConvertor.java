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

import com.github.loadup.components.gateway.facade.model.SecurityConfigDto;
import com.github.loadup.components.gateway.plugin.repository.database.dal.dataobject.SecurityDO;

/**
 *
 */
public class SecurityConvertor {
    /**
     * SecurityConfigDto to SecurityDO
     */
    public static SecurityDO dto2DO(SecurityConfigDto dto) {
        if (dto == null) {
            return null;
        }
        SecurityDO securityDO = new SecurityDO();
        securityDO.setAlgoName(dto.getAlgoName());
        securityDO.setSecurityStrategyCode(dto.getSecurityStrategyCode());
        securityDO.setClientId(dto.getClientId());
        securityDO.setOperateType(dto.getOperateType());
        securityDO.setCertContent(dto.getCertContent());
        securityDO.setKeyType(dto.getKeyType());
        securityDO.setCertType(dto.getCertType());
        securityDO.setProperties(dto.getProperties());
        securityDO.setAlgoProperties(dto.getProperties());
        securityDO.setStatus(dto.getStatus());
        securityDO.setGmtCreate(dto.getGmtCreate());
        securityDO.setGmtModified(dto.getGmtModified());
        securityDO.setGmtValid(null);
        securityDO.setGmtInvalid(null);
        return securityDO;
    }

    /**
     * SecurityDO to SecurityConfigDto
     */
    public static SecurityConfigDto DO2CertConfigInnerDto(SecurityDO securityDO) {
        SecurityConfigDto configDto = new SecurityConfigDto();
        if (securityDO == null) {
            return configDto;
        }
        configDto.setAlgoName(securityDO.getAlgoName());
        configDto.setSecurityStrategyCode(securityDO.getSecurityStrategyCode());
        configDto.setClientId(securityDO.getClientId());
        configDto.setOperateType(securityDO.getOperateType());
        configDto.setCertContent(securityDO.getCertContent());
        configDto.setKeyType(securityDO.getKeyType());
        configDto.setCertType(securityDO.getCertType());
        configDto.setProperties(securityDO.getProperties());
        configDto.setAlgoProperties(securityDO.getAlgoProperties());
        configDto.setStatus(securityDO.getStatus());
        configDto.setGmtCreate(securityDO.getGmtCreate());
        configDto.setGmtModified(securityDO.getGmtModified());
        configDto.setGmtValid(securityDO.getGmtValid());
        configDto.setGmtInvalid(securityDO.getGmtInvalid());
        return configDto;
    }
}
