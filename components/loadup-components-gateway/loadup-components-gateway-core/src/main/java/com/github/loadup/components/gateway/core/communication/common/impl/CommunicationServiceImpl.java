package com.github.loadup.components.gateway.core.communication.common.impl;

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

import com.github.loadup.components.gateway.core.communication.common.facade.CommunicationService;
import com.github.loadup.components.gateway.core.communication.common.model.MessageSendResult;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.core.model.communication.TransportProtocol;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 通信api服务实现
 */
@Component("communicationService")
public class CommunicationServiceImpl implements CommunicationService {

    /**
     * 处理器
     */
    private static final Map<String, CommunicationService> handler = new HashMap<>();

    /**
     * 注册监听器
     */
    public static void registerHandler(String protocol, CommunicationService service) {
        handler.put(protocol, service);
    }

    /**
     * Send message send result.
     */
    @Override
    public MessageSendResult send(String traceId, CommunicationConfig communicationConfig,
                                MessageEnvelope messageEnvelope) {
        String protocol = communicationConfig.getProtocol();

        CommunicationService service = handler.get(protocol.toUpperCase());
        //如果为空，默认使用代理方式运行
        if (service == null) {
            service = handler.get(TransportProtocol.PROXY);
        }
        return service.send(traceId, communicationConfig, messageEnvelope);
    }

    /**
     *
     */
    @Override
    public void init(Object... obj) {
        for (String key : handler.keySet()) {
            CommunicationService service = handler.get(key);
            service.init(obj);
        }

    }

    /**
     *
     */
    @Override
    public void refresh(Object... obj) {
        for (String key : handler.keySet()) {
            CommunicationService service = handler.get(key);
            service.refresh(obj);
        }
    }

    /**
     *
     */
    @Override
    public void refreshById(String id, Object... obj) {
        for (String key : handler.keySet()) {
            CommunicationService service = handler.get(key);
            service.refreshById(id, obj);
        }
    }

    /**
     *
     */
    @Override
    public boolean isInitOk() {
        boolean result = true;
        for (String key : handler.keySet()) {
            CommunicationService service = handler.get(key);
            result = result && service.isInitOk();
        }
        return result;
    }

}
