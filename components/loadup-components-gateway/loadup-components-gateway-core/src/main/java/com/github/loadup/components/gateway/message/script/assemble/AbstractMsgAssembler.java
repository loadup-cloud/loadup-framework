package com.github.loadup.components.gateway.message.script.assemble;

import com.github.loadup.components.gateway.common.exception.ErrorCode;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.common.enums.MessageFormat;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.message.base.assemble.MessageAssembler;
import com.github.loadup.components.gateway.message.common.errorr.ParserErrorCode;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMsgAssembler implements MessageAssembler {
    private final Logger logger = LoggerFactory.getLogger(AbstractMsgAssembler.class);

    protected abstract Object assembleMessage(UnifyMsg message);

    protected abstract Object assembleErrorMessage(UnifyMsg message, GatewayException exception);

    @Override
    public MessageEnvelope assemble(UnifyMsg message) {
        Object content;
        try {
            content = assembleMessage(message);
        } catch (GatewayException e) {
            LogUtil.error(logger, e, "Message assemble fail!");
            content = assembleErrorMessage(message, e);
        } catch (Exception e) {
            LogUtil.error(logger, e, "Message assemble fail!");
            ErrorCode errorCode = ParserErrorCode.PARSE_ERROR;
            throw new GatewayException(errorCode, e);
        }

        return new MessageEnvelope(MessageFormat.TEXT, content);
    }

    @Override
    public MessageEnvelope assembleError(UnifyMsg message, GatewayException exception) {
        Object content;
        try {
            content = assembleErrorMessage(message, exception);
        } catch (GatewayException e) {
            LogUtil.error(logger, e, "Message assemble fail!");
            throw e;
        } catch (Exception e) {
            LogUtil.error(logger, e, "Message assemble fail!");
            ErrorCode errorCode = ParserErrorCode.PARSE_ERROR;
            throw new GatewayException(errorCode, e);
        }

        return new MessageEnvelope(MessageFormat.TEXT, content);
    }

}
