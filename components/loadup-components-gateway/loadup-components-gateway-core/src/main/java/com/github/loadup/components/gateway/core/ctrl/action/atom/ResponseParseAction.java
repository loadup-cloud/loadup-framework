package com.github.loadup.components.gateway.core.ctrl.action.atom;

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