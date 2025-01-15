package com.github.loadup.components.gateway.message.script.parser;

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

import com.github.loadup.components.gateway.common.exception.ErrorCode;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.message.common.errorr.ParserErrorCode;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants.KEY_PARSE_RESULT;

public abstract class AbstractMsgParser implements MessageParser {

	protected abstract String parseMessage(String message, Map<String, String> httpHeaderInfo);

	protected static final Logger logger = LoggerFactory.getLogger(AbstractMsgParser.class);

	@Override
	public UnifyMsg parse(MessageEnvelope messageEnvelope) {
		UnifyMsg messageResult = new UnifyMsg();

		String message = String.valueOf(messageEnvelope.getContent());
		Map<String, String> extInfo = messageEnvelope.getHeaders();
		String parseResult;
		try {
			parseResult = parseMessage(message, extInfo);
		} catch (GatewayException e) {
			LogUtil.error(logger, e, "Message parser fail!");
			throw e;
		} catch (Exception e) {
			LogUtil.error(logger, e, "Message parser fail!");
			ErrorCode errorCode = ParserErrorCode.PARSE_ERROR;
			throw new GatewayException(errorCode, e);
		}
		messageResult.addField(KEY_PARSE_RESULT, parseResult);
		return messageResult;
	}
}
