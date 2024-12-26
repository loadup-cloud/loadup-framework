package com.github.loadup.components.gateway.plugin.repository.database.config;

import com.github.loadup.components.gateway.facade.model.CommunicationConfigDto;
import com.github.loadup.components.gateway.repository.common.AbstractInterfaceConfigBuilder;
import org.springframework.stereotype.Component;

import static com.github.loadup.components.gateway.core.common.Constant.UNDERSCORE;

/**
 * CommunicationConfig Builder
 */
@Component("databaseCommunicationConfigBuilder")
public class CommunicationConfigBuilder extends
        AbstractInterfaceConfigBuilder<CommunicationConfigDto> {

    /**
     * generic config build
     * format should follow the template which is defined in SPI interface
     */
    public CommunicationConfigDto build(String url, String interfaceId, String securityStrategyCode,
                                        String communicationProperties, int index) {
        if (!validate(url, securityStrategyCode)) {
            return null;
        }
        CommunicationConfigDto communicationConfigDto = new CommunicationConfigDto();
        String protocol = getProtocol(url);
        //interfaceId_index
        communicationConfigDto
                .setCommunicationId(interfaceId.concat(UNDERSCORE).concat(String.valueOf(index)));
        communicationConfigDto.setInterfaceId(interfaceId);
        communicationConfigDto.setUri(url);
        communicationConfigDto.setProperties(communicationProperties);
        communicationConfigDto.setProtocol(protocol);
        return communicationConfigDto;
    }

}
