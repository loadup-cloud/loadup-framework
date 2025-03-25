package com.github.loadup.components.gateway.core.service.impl;

/*-
 * #%L
 * loadup-components-gateway-core
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

import com.github.loadup.components.gateway.cache.manager.CacheManager;
import com.github.loadup.components.gateway.core.model.*;
import com.github.loadup.components.gateway.core.service.InterfaceConfigInnerService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Component("gatewayInterfaceConfigInnerService")
public class InterfaceConfigInnerServiceImpl implements InterfaceConfigInnerService {

    /**
     * cache manager service
     */
    @Resource
    @Qualifier("gatewayCacheManager")
    private CacheManager cacheManager;

    /**
     * @see InterfaceConfigInnerService#putApiConfigsToCache(java.util.List, java.util.List, java.util.List, java.util.List, java.util.List)
     */
    @Override
    public void putApiConfigsToCache(List<InterfaceConfig> interfaceConfigList,
                                     List<MessageSenderConfig> messageSenderConfigList,
                                     List<MessageReceiverConfig> messageReceiverConfigList,
                                     List<MessageProcessConfig> messageProcessConfigList,
                                     List<CommunicationConfig> communicationConfigList) {
        // put to cache
        cacheManager.pushToCache(interfaceConfigList, messageProcessConfigList,
                communicationConfigList, messageReceiverConfigList, messageSenderConfigList, null,
                null, null);
    }

    /**
     * @see InterfaceConfigInnerService#putSpiConfigsToCache(java.util.List, java.util.List, java.util.List, java.util.List, java.util.List)
     */
    @Override
    public void putSpiConfigsToCache(List<InterfaceConfig> interfaceConfigList,
                                     List<MessageSenderConfig> messageSenderConfigList,
                                     List<MessageReceiverConfig> messageReceiverConfigList,
                                     List<MessageProcessConfig> messageProcessConfigList,
                                     List<CommunicationConfig> communicationConfigList) {
        // put to cache
        cacheManager.pushToCache(interfaceConfigList, messageProcessConfigList,
                communicationConfigList, messageReceiverConfigList, messageSenderConfigList, null,
                null, null);

    }

}
