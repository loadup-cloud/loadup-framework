package com.github.loadup.components.gateway.plugin.repository.file.config;

import com.github.loadup.components.gateway.facade.model.InstConfigDto;
import com.github.loadup.components.gateway.repository.common.AbstractInterfaceConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * inst config builder
 */
@Component("gatewayFileInstConfigBuilder")
public class InstConfigBuilder extends AbstractInterfaceConfigBuilder<InstConfigDto> {

    private static final Logger logger = LoggerFactory
            .getLogger(InstConfigBuilder.class);

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
