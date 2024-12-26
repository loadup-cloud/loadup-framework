package com.github.loadup.components.gateway.message.script.parser;

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
