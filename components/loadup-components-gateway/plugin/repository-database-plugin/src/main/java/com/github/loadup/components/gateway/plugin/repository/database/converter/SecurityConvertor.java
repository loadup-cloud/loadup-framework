package com.github.loadup.components.gateway.plugin.repository.database.converter;

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