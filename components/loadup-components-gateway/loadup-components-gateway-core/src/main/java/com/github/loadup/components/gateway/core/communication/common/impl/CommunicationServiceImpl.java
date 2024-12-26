package com.github.loadup.components.gateway.core.communication.common.impl;

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
     * 注册监听器
     */
    public static void registerHandler(String protocol, CommunicationService service) {
        handler.put(protocol, service);
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
