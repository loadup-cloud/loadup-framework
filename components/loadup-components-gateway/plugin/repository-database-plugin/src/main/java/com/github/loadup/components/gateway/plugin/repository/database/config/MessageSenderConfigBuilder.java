package com.github.loadup.components.gateway.plugin.repository.database.config;

import com.github.loadup.components.gateway.facade.model.MessageSenderConfigDto;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.repository.common.AbstractInterfaceConfigBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.github.loadup.components.gateway.core.common.Constant.URI_SEPARATOR;

/**
 * message sender config builder
 */
@Component("databaseMessageSenderConfigBuilder")
public class MessageSenderConfigBuilder extends
        AbstractInterfaceConfigBuilder<MessageSenderConfigDto> {

    private static final Logger logger = LoggerFactory
            .getLogger(MessageSenderConfigBuilder.class);

    /**
     * @see AbstractInterfaceConfigBuilder#validate(String, String)
     */
    @Override
    protected boolean validate(String url, String securityStrategyCode) {
        boolean isValidUri = super.validate(url, securityStrategyCode);

        if (isValidUri) {
            if (StringUtils.contains(url, URI_SEPARATOR)) {
                String domainPath = StringUtils.substringAfter(url, URI_SEPARATOR);

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
        if (!this.validate(url, securityStrategyCode)) {
            return null;
        }

        String domainString = getDomain(url);

        MessageSenderConfigDto msgSender = new MessageSenderConfigDto();
        msgSender.setMessageSenderId(domainString);
        msgSender.setMessageSenderName(domainString);
        msgSender.setCertCode(securityStrategyCode);

        return msgSender;
    }

}
