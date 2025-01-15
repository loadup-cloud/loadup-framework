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
