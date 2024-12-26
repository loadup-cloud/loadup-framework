package com.github.loadup.components.gateway.core.ctrl.action.atom;

import com.github.loadup.components.gateway.core.communication.common.facade.CommunicationService;
import com.github.loadup.components.gateway.core.communication.common.model.MessageSendResult;
import com.github.loadup.components.gateway.core.ctrl.action.AbstractBusinessAction;
import com.github.loadup.components.gateway.core.ctrl.action.BusinessAction;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * <p>
 * ReceiveParseAction.java
 * </p>
 */
@Component("sendToIntegratorAction")
public class SendToIntegratorAction extends AbstractBusinessAction {

    @Resource
    private CommunicationService communicationService;

    @Override
    public void doBusiness(GatewayRuntimeProcessContext gatewayRuntimeProcessContext) {
        String traceId = gatewayRuntimeProcessContext.getTraceId();
        CommunicationConfig communicationConfig = gatewayRuntimeProcessContext.getIntegratorCommunicationConfig();
        MessageEnvelope requestMessage = gatewayRuntimeProcessContext.getResultMessage();
        MessageSendResult messageSendResult = communicationService.send(traceId, communicationConfig, requestMessage);
        gatewayRuntimeProcessContext.setResultMessage(messageSendResult.getMessageEnvelope());

    }

    @Override
    @Resource
    @Qualifier("responseParseAction")
    public void setNextAction(BusinessAction responseParseAction) {
        this.nextAction = responseParseAction;
    }

}