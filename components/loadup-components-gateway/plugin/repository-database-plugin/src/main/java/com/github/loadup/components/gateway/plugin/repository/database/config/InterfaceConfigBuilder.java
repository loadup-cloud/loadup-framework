package com.github.loadup.components.gateway.plugin.repository.database.config;

import com.github.loadup.components.gateway.core.common.enums.InterfaceStatus;
import com.github.loadup.components.gateway.facade.model.InterfaceConfigDto;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.repository.common.AbstractInterfaceConfigBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Interface Config Builder
 */
@Component("databaseInterfaceConfigBuilder")
public class InterfaceConfigBuilder extends AbstractInterfaceConfigBuilder<InterfaceConfigDto> {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(InterfaceConfigBuilder.class);

    /**
     * generic config build
     */
    public InterfaceConfigDto build(String interfaceId, String securityStrategyCode,
                                    String integrationInterfaceId, String interfaceName,
                                    String version, String status, String communicationProperties) {

        if (StringUtils.isBlank(interfaceId) || StringUtils.isBlank(securityStrategyCode)) {

            LogUtil.error(logger, "There are some invalid interfaceId=" + interfaceId
                    + ";securityStrategyCode=" + securityStrategyCode);
            return null;
        }

        InterfaceConfigDto interfaceConfigDto = new InterfaceConfigDto();

        interfaceConfigDto.setInterfaceId(interfaceId);
        interfaceConfigDto.setMessageProcessId(interfaceId);
        interfaceConfigDto.setInterfaceName(interfaceName);
        interfaceConfigDto.setVersion(version);
        interfaceConfigDto.setStatus(status);
        interfaceConfigDto.setCertCode(securityStrategyCode);
        interfaceConfigDto
                .setIsEnable(InterfaceStatus.VALID.getCode().equals(status) ? "true" : "false");
        if (StringUtils.isNotEmpty(integrationInterfaceId)) {
            interfaceConfigDto.setMessageReceiverInterfaceId(integrationInterfaceId);
        }
        interfaceConfigDto.setProperties(communicationProperties);
        return interfaceConfigDto;
    }
}
