package com.github.loadup.components.gateway.message.script.parser;

import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;

/**
 * 报文解析器
 */
public interface MessageParser {

    /**
     * 解析报文
     */
    UnifyMsg parse(MessageEnvelope messageEnvelope);

}
