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

import com.github.loadup.components.gateway.core.common.annotation.LogTraceId;
import com.github.loadup.components.gateway.core.common.enums.RoleType;
import com.github.loadup.components.gateway.core.communication.http.util.HttpConfigUtil;
import com.github.loadup.components.gateway.core.ctrl.action.AbstractBusinessAction;
import com.github.loadup.components.gateway.core.ctrl.action.BusinessAction;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.model.InterfaceConfig;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.core.model.communication.TransportURI;
import com.github.loadup.components.gateway.message.base.api.MessageEngine;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsgHelper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * <p>
 * ReceiveParseAction.java
 * </p>
 */
@Component("requestAssembleAction")
public class RequestAssembleAction extends AbstractBusinessAction {

    @Resource
    @Qualifier("messageEngine")
    private MessageEngine messageEngine;

    @LogTraceId
    @Override
    public void doBusiness(GatewayRuntimeProcessContext context) {
        InterfaceConfig integratorInterfaceConfig = context.getIntegratorInterfaceConfig();
        UnifyMsg unifyMsg = null;
        MessageEnvelope inputResult = context.getResultMessage();
        switch (inputResult.getMessageFormat()) {
            case TEXT:
                unifyMsg = UnifyMsgHelper.fromJSonStr(String.valueOf(inputResult.getContent()));
                if (unifyMsg == null) {
                    unifyMsg = new UnifyMsg();
                }
                break;
            case MAP:
                unifyMsg = new UnifyMsg();
                Map<String, String> message = (Map) inputResult.getContent();
                if (!CollectionUtils.isEmpty(message)) {
                    UnifyMsg finalUnifyMsg = unifyMsg;
                    message.forEach((k, v) -> {
                        finalUnifyMsg.addField(k, v);
                    });
                }
                break;
            default:
                break;
        }
        CommunicationConfig communicationConfig = context.getIntegratorCommunicationConfig();
        CommunicationConfig cloneConfig = communicationConfig.clone();
        if (HttpConfigUtil.isRest(cloneConfig)) {
            reAssembleURI(cloneConfig, unifyMsg);
            context.setIntegratorCommunicationConfig(cloneConfig);
        }
        String interfaceId = integratorInterfaceConfig.getInterfaceId();
        MessageEnvelope msgEnvelope = messageEngine.assemble(interfaceId, RoleType.RECEIVER, context,
                context.getTransactionType(), unifyMsg);
        context.setResultMessage(msgEnvelope);
    }

    private void reAssembleURI(CommunicationConfig cloneConfig, UnifyMsg unifyMsg) {
        String origUrl = cloneConfig.getUri().getUrl();
        String url = origUrl;
        if (StringUtils.contains(origUrl, "$!m.g") || StringUtils.contains(origUrl, "$m.g")) {
            int firstIndex = StringUtils.indexOf(origUrl, "$!");
            int lastIndex = StringUtils.lastIndexOf(origUrl, "')");
            String prefixPart = StringUtils.substring(origUrl, 0, firstIndex);
            String suffixPart = StringUtils.substring(origUrl, lastIndex + 2);
            String translatePart = StringUtils.substring(origUrl, firstIndex, lastIndex + 2);
            //            String middlePart = messageAssembler.assemble(translatePart, unifyMsg);
            url = prefixPart + "middlePart" + suffixPart;
        }
        String schema = cloneConfig.getUri().getSchema();
        TransportURI transportURI = new TransportURI(url, schema);
        cloneConfig.setUri(transportURI);
    }

    @Override
    @Resource
    @Qualifier("limitCheckAction")
    public void setNextAction(BusinessAction sendToIntegrationAction) {
        this.nextAction = sendToIntegrationAction;
    }

}
