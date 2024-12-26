package com.github.loadup.components.gateway.plugin.repository.file.config;

import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.facade.model.MessageSenderConfigDto;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.plugin.repository.file.model.ApiConfigRepository;
import com.github.loadup.components.gateway.repository.common.AbstractInterfaceConfigBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * message sender config builder
 */
@Component("gatewayFileMessageSenderConfigBuilder")
public class MessageSenderConfigBuilder extends AbstractInterfaceConfigBuilder<MessageSenderConfigDto> {

    private static final Logger logger = LoggerFactory.getLogger(MessageSenderConfigBuilder.class);

    /**
     * validate mandatory fields
     */
    @Override
    protected boolean validate(String url, String securityStrategyCode) {
        boolean isValidUri = super.validate(url, securityStrategyCode);

        if (isValidUri) {
            if (StringUtils.contains(url, Constant.URI_SEPARATOR)) {

                String domainPath = StringUtils.substringAfter(url, Constant.URI_SEPARATOR);

                if (StringUtils.isBlank(domainPath)) {
                    LogUtil.error(logger, "Ignore invalid URI string=" + url);
                    return false;
                }
            }
        }

        return isValidUri;

    }

    /**
     * generic config build
     */
    public MessageSenderConfigDto build(String url, String securityStrategyCode) {
        if (!validate(url, securityStrategyCode)) {
            return null;
        }

        String domainString = getDomain(url);

        MessageSenderConfigDto msgSender = new MessageSenderConfigDto();
        msgSender.setMessageSenderId(domainString);
        msgSender.setMessageSenderName(domainString);
        msgSender.setCertCode(securityStrategyCode);

        return msgSender;
    }

    public MessageSenderConfigDto build(ApiConfigRepository apiConfig) {
        String domainString = getURIPathWithUrl(apiConfig.getOpenURl());
        MessageSenderConfigDto msgSender = new MessageSenderConfigDto();
        msgSender.setMessageSenderId(domainString);
        msgSender.setMessageSenderName(domainString);
        msgSender.setCertCode(apiConfig.getSecurityStrategyCode());
        return msgSender;
    }

}
