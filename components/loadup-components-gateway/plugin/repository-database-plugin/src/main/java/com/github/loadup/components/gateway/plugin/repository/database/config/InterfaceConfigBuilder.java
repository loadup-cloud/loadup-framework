package com.github.loadup.components.gateway.plugin.repository.database.config;

/*-
 * #%L
 * repository-database-plugin
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
    private static final Logger logger = LoggerFactory.getLogger(InterfaceConfigBuilder.class);

    /**
     * generic config build
     */
    public InterfaceConfigDto build(
            String interfaceId,
            String securityStrategyCode,
            String integrationInterfaceId,
            String interfaceName,
            String version,
            String status,
            String communicationProperties) {

        if (StringUtils.isBlank(interfaceId) || StringUtils.isBlank(securityStrategyCode)) {

            LogUtil.error(
                    logger,
                    "There are some invalid interfaceId=" + interfaceId + ";securityStrategyCode="
                            + securityStrategyCode);
            return null;
        }

        InterfaceConfigDto interfaceConfigDto = new InterfaceConfigDto();

        interfaceConfigDto.setInterfaceId(interfaceId);
        interfaceConfigDto.setMessageProcessId(interfaceId);
        interfaceConfigDto.setInterfaceName(interfaceName);
        interfaceConfigDto.setVersion(version);
        interfaceConfigDto.setStatus(status);
        interfaceConfigDto.setCertCode(securityStrategyCode);
        interfaceConfigDto.setIsEnable(InterfaceStatus.VALID.getCode().equals(status) ? "true" : "false");
        if (StringUtils.isNotEmpty(integrationInterfaceId)) {
            interfaceConfigDto.setMessageReceiverInterfaceId(integrationInterfaceId);
        }
        interfaceConfigDto.setProperties(communicationProperties);
        return interfaceConfigDto;
    }
}
