package com.github.loadup.components.gateway.message.base.assemble;

import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;

/**
 * vm报文组装引擎
 */
public interface MessageAssembler {

    /**
     * 组装报文服务
     */
    MessageEnvelope assemble(UnifyMsg message);

    /**
     * 组装异常报文服务
     */
    MessageEnvelope assembleError(UnifyMsg message, GatewayException exception);

}
