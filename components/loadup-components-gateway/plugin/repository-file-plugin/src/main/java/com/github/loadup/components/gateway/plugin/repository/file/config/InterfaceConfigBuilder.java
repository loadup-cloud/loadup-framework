package com.github.loadup.components.gateway.plugin.repository.file.config;

import com.github.loadup.components.gateway.facade.model.InterfaceConfigDto;
import com.github.loadup.components.gateway.repository.common.AbstractInterfaceConfigBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Interface Config Builder
 */
@Component("gatewayFileInterfaceConfigBuilder")
public class InterfaceConfigBuilder extends AbstractInterfaceConfigBuilder<InterfaceConfigDto> {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(InterfaceConfigBuilder.class);

    /**
     * generic config build
     */
    public InterfaceConfigDto build(String url, String securityStrategyCode,
                                    String communicationProperties, String integrationInterfaceId) {
        if (!validate(url, securityStrategyCode)) {
            return null;
        }

        //1 interfaceId
        String interfaceId = generateBizKey(url);

        InterfaceConfigDto interfaceConfigDto = new InterfaceConfigDto();
        interfaceConfigDto.setInterfaceId(interfaceId);
        interfaceConfigDto.setMessageProcessId(interfaceId);
        interfaceConfigDto.setInterfaceName(interfaceId);
        interfaceConfigDto.setCertCode(securityStrategyCode);
        interfaceConfigDto.setIsEnable("true");
        if (StringUtils.isNotEmpty(integrationInterfaceId)) {
            interfaceConfigDto
                    .setMessageReceiverInterfaceId(generateBizKey(integrationInterfaceId));
        }
        interfaceConfigDto.setProperties(communicationProperties);
        return interfaceConfigDto;
    }
}
