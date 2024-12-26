package com.github.loadup.components.gateway.plugin.repository.database.config;

import com.github.loadup.components.gateway.facade.model.InstConfigDto;
import com.github.loadup.components.gateway.repository.common.AbstractInterfaceConfigBuilder;
import org.springframework.stereotype.Component;

/**
 * inst config builder
 */
@Component("gatewayDatabaseInstConfigBuilder")
public class InstConfigBuilder extends AbstractInterfaceConfigBuilder<InstConfigDto> {

    /**
     * generic config build
     */
    public InstConfigDto build(String clientId, String name, String properties) {
        InstConfigDto instConfigDto = new InstConfigDto();
        instConfigDto.setClientId(clientId);
        instConfigDto.setName(name);
        instConfigDto.setProperties(properties);
        return instConfigDto;
    }

}
