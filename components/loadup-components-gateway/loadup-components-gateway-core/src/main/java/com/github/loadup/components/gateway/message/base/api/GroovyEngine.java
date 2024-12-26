package com.github.loadup.components.gateway.message.base.api;

import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.common.enums.RoleType;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;

public interface GroovyEngine {

    UnifyMsg parse(String beanName, RoleType roleType, String interfaceTypeStr, MessageEnvelope messageEnvelope);

    MessageEnvelope assemble(String beanName, RoleType roleType, String interfaceTypeStr, UnifyMsg message);

    MessageEnvelope assembleErrorMessage(String beanName, RoleType roleType, String interfaceTypeStr, UnifyMsg message,
                                         GatewayException exception);

}
