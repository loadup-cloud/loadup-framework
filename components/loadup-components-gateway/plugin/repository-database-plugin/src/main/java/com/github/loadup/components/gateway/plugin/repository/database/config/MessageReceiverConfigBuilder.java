package com.github.loadup.components.gateway.plugin.repository.database.config;

import com.github.loadup.components.gateway.facade.model.MessageReceiverConfigDto;
import com.github.loadup.components.gateway.repository.common.AbstractInterfaceConfigBuilder;
import org.springframework.stereotype.Component;

/**
 * MessageReceiver Builder
 */
@Component("databaseMessageReceiverConfigBuilder")
public class MessageReceiverConfigBuilder extends
        AbstractInterfaceConfigBuilder<MessageReceiverConfigDto> {

    /**
     * generic config build
     */
    public MessageReceiverConfigDto build(String url, String securityStrategyCode) {
        if (!validate(url, securityStrategyCode)) {
            return null;
        }
        String domain = getDomain(url);

        MessageReceiverConfigDto msgReceiver = new MessageReceiverConfigDto();
        msgReceiver.setMessageReceiverId(domain);
        msgReceiver.setMessageReceiverName(domain);
        msgReceiver.setCertCode(securityStrategyCode);
        return msgReceiver;
    }
}
