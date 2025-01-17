package com.github.loadup.components.gateway.core.ctrl.action.atom;

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

import com.github.loadup.components.gateway.core.common.enums.RoleType;
import com.github.loadup.components.gateway.core.ctrl.action.AbstractBusinessAction;
import com.github.loadup.components.gateway.core.ctrl.action.BusinessAction;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.message.base.api.MessageEngine;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants.KEY_PARSE_EXCEPTION;
import static com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants.KEY_PARSE_RESULT;

/**
 * <p>
 * ReceiveParseAction.java
 * </p>
 */
@Component("responseParseAction")
public class ResponseParseAction extends AbstractBusinessAction {

    @Resource
    private MessageEngine messageEngine;

    @Override
    public void doBusiness(GatewayRuntimeProcessContext gatewayRuntimeProcessContext) {
        MessageEnvelope resultEnvelope = gatewayRuntimeProcessContext.getResultMessage();
        String integratorInterfaceId = gatewayRuntimeProcessContext.getIntegratorInterfaceConfig().getInterfaceId();
        UnifyMsg unifyCommonMessage = new UnifyMsg();
        UnifyMsg respMessage = messageEngine.parse(integratorInterfaceId, RoleType.RECEIVER, gatewayRuntimeProcessContext,
                resultEnvelope);
        String exceptionMsg = respMessage.g(KEY_PARSE_EXCEPTION);
        String parseResult = respMessage.g(KEY_PARSE_RESULT);

        MessageEnvelope responseEnvelope = resultEnvelope.clone();
        responseEnvelope.setHeaders(resultEnvelope.getHeaders());
        responseEnvelope.setContent(StringUtils.isBlank(exceptionMsg) ? parseResult : exceptionMsg);
        MessageEnvelope result = responseEnvelope.clone();
        gatewayRuntimeProcessContext.setResultMessage(result);
        gatewayRuntimeProcessContext.setResponseMessage(responseEnvelope);
    }

    @Override
    @Resource
    @Qualifier("limitReleaseAction")
    public void setNextAction(BusinessAction responseAssembleAction) {
        this.nextAction = responseAssembleAction;
    }
}
