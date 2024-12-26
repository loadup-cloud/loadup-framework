package com.github.loadup.components.gateway.message.base.api;

import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.common.enums.RoleType;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;

/**
 * Message Engine
 */
public interface MessageEngine {

    UnifyMsg parse(String interfaceId,
                   RoleType roleType,
                   GatewayRuntimeProcessContext context,
                   MessageEnvelope messageEnvelope);

    MessageEnvelope assemble(String interfaceId,
                             RoleType roleType,
                             GatewayRuntimeProcessContext context,
                             String interfaceTypeStr,
                             UnifyMsg message);

    /**
     * assemble exception message
     */
    MessageEnvelope assembleErrorMessage(String interfaceId,
                                         RoleType roleType,
                                         GatewayRuntimeProcessContext context,
                                         String interfaceTypeStr,
                                         UnifyMsg message,
                                         MessageEnvelope messageEnvelope,
                                         GatewayException exception);

}
