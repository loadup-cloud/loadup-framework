package com.github.loadup.components.gateway.plugin.repository.file.config;

/*-
 * #%L
 * repository-file-plugin
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
public class CommunicationConfigBuilder extends AbstractInterfaceConfigBuilder<CommunicationConfigDto> {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(CommunicationConfigBuilder.class);

    /**
     * generic config builder
     */
    public CommunicationConfigDto build(
            String url, String securityStrategyCode, String communicationProperties, int index) {
        if (!validate(url, securityStrategyCode)) {
            return null;
        }
        CommunicationConfigDto communicationConfigDto = new CommunicationConfigDto();
        String interfaceId = generateBizKey(url);
        String protocol = getProtocol(url);
        // interfaceId_index
        communicationConfigDto.setCommunicationId(
                interfaceId.concat(Constant.UNDERSCORE).concat(String.valueOf(index)));
        communicationConfigDto.setInterfaceId(interfaceId);
        communicationConfigDto.setUri(url);
        communicationConfigDto.setProperties(communicationProperties);
        communicationConfigDto.setProtocol(protocol);
        return communicationConfigDto;
    }
}
