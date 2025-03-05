package com.github.loadup.components.gateway.message.script.assemble;

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

import com.github.loadup.commons.error.CommonException;
import com.github.loadup.commons.result.ResultCode;
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

    protected abstract Object assembleErrorMessage(UnifyMsg message, CommonException exception);

    @Override
    public MessageEnvelope assemble(UnifyMsg message) {
        Object content;
        try {
            content = assembleMessage(message);
        } catch (CommonException e) {
            LogUtil.error(logger, e, "Message assemble fail!");
            content = assembleErrorMessage(message, e);
        } catch (Exception e) {
            LogUtil.error(logger, e, "Message assemble fail!");
            ResultCode errorCode = ParserErrorCode.PARSE_ERROR;
            throw new CommonException(errorCode, e);
        }

        return new MessageEnvelope(MessageFormat.TEXT, content);
    }

    @Override
    public MessageEnvelope assembleError(UnifyMsg message, CommonException exception) {
        Object content;
        try {
            content = assembleErrorMessage(message, exception);
        } catch (CommonException e) {
            LogUtil.error(logger, e, "Message assemble fail!");
            throw e;
        } catch (Exception e) {
            LogUtil.error(logger, e, "Message assemble fail!");
            ResultCode errorCode = ParserErrorCode.PARSE_ERROR;
            throw new CommonException(errorCode, e);
        }

        return new MessageEnvelope(MessageFormat.TEXT, content);
    }
}
