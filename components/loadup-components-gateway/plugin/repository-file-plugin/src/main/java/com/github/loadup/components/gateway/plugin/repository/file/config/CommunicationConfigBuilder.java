package com.github.loadup.components.gateway.plugin.repository.file.config;

import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.facade.model.CommunicationConfigDto;
import com.github.loadup.components.gateway.repository.common.AbstractInterfaceConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CommunicationConfig Builder
 */
@Component("gatewayFileCommunicationConfigBuilder")
public class CommunicationConfigBuilder extends
        AbstractInterfaceConfigBuilder<CommunicationConfigDto> {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(CommunicationConfigBuilder.class);

    /**
     * generic config builder
     */
    public CommunicationConfigDto build(String url, String securityStrategyCode,
                                        String communicationProperties, int index) {
        if (!validate(url, securityStrategyCode)) {
            return null;
        }
        CommunicationConfigDto communicationConfigDto = new CommunicationConfigDto();
        String interfaceId = generateBizKey(url);
        String protocol = getProtocol(url);
        //interfaceId_index
        communicationConfigDto
                .setCommunicationId(interfaceId.concat(Constant.UNDERSCORE).concat(String.valueOf(index)));
        communicationConfigDto.setInterfaceId(interfaceId);
        communicationConfigDto.setUri(url);
        communicationConfigDto.setProperties(communicationProperties);
        communicationConfigDto.setProtocol(protocol);
        return communicationConfigDto;
    }
}
