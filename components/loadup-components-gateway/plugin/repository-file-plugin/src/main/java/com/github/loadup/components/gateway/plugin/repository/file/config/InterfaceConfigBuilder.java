/* Copyright (C) LoadUp Cloud 2022-2025 */
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

import com.github.loadup.components.gateway.facade.model.InterfaceConfigDto;
import com.github.loadup.components.gateway.plugin.repository.file.model.ApiConfigRepository;
import com.github.loadup.components.gateway.repository.common.AbstractInterfaceConfigBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface Config Builder
 */
@Component("gatewayFileInterfaceConfigBuilder")
public class InterfaceConfigBuilder extends AbstractInterfaceConfigBuilder<InterfaceConfigDto> {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(InterfaceConfigBuilder.class);

    /**
     * generic config build
     */
    public InterfaceConfigDto build(
            String url, String securityStrategyCode, String communicationProperties, String integrationInterfaceId) {
        if (!validate(url, securityStrategyCode)) {
            return null;
        }

        // 1 interfaceId
        String interfaceId = generateBizKey(url);

        InterfaceConfigDto interfaceConfigDto = new InterfaceConfigDto();
        interfaceConfigDto.setInterfaceId(interfaceId);
        interfaceConfigDto.setMessageProcessId(interfaceId);
        interfaceConfigDto.setInterfaceName(interfaceId);
        interfaceConfigDto.setCertCode(securityStrategyCode);
        interfaceConfigDto.setIsEnable("true");
        if (StringUtils.isNotEmpty(integrationInterfaceId)) {
            interfaceConfigDto.setMessageReceiverInterfaceId(generateBizKey(integrationInterfaceId));
        }
        interfaceConfigDto.setProperties(communicationProperties);
        return interfaceConfigDto;
    }

    public List<InterfaceConfigDto> build(ApiConfigRepository apiConfig) {
        List<InterfaceConfigDto> list = new ArrayList<>();
        list.add(build(apiConfig.getOpenURl(), apiConfig.getSecurityStrategyCode(), apiConfig.getCommunicationProperties(),
                apiConfig.getIntegrationUri()));
        list.add(build(apiConfig.getIntegrationUri(), apiConfig.getSecurityStrategyCode(), apiConfig.getCommunicationProperties(),
                apiConfig.getIntegrationUri()));
        return list;
    }
}
