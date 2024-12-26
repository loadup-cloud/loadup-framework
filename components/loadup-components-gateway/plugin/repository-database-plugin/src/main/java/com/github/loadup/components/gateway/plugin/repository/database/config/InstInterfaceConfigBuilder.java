package com.github.loadup.components.gateway.plugin.repository.database.config;

import com.github.loadup.components.gateway.facade.model.InstConfigDto;
import com.github.loadup.components.gateway.facade.model.InstInterfaceConfigDto;
import com.github.loadup.components.gateway.repository.common.AbstractInterfaceConfigBuilder;
import org.springframework.stereotype.Component;

/**
 * inst interface config builder
 */
@Component("databaseInstInterfaceConfigBuilder")
public class InstInterfaceConfigBuilder extends AbstractInterfaceConfigBuilder<InstConfigDto> {

    /**
     * generic config build
     */
    public InstInterfaceConfigDto build(String clientId, String interfaceId) {
        InstInterfaceConfigDto instConfigDto = new InstInterfaceConfigDto();
        instConfigDto.setClientId(clientId);
        instConfigDto.setInterfaceId(interfaceId);
        return instConfigDto;
    }

}
