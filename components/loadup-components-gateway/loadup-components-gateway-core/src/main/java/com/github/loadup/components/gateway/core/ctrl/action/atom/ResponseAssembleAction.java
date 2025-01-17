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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.loadup.commons.error.CommonException;
import com.github.loadup.components.gateway.common.util.CommonUtil;
import com.github.loadup.components.gateway.common.util.DateUtil;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.core.common.enums.RoleType;
import com.github.loadup.components.gateway.core.ctrl.action.AbstractBusinessAction;
import com.github.loadup.components.gateway.core.ctrl.action.BusinessAction;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.core.model.InterfaceConfig;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.core.prototype.model.SignatureRequest;
import com.github.loadup.components.gateway.core.prototype.parser.SignatureService;
import com.github.loadup.components.gateway.message.base.api.MessageEngine;
import com.github.loadup.components.gateway.message.script.cache.GroovyScriptCache;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

import static com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants.*;

/**
 * <p>
 * ReceiveParseAction.java
 * </p>
 */
@Component("responseAssembleAction")
public class ResponseAssembleAction extends AbstractBusinessAction {

    /**
     * client-id is dispatch to the merchant
     */
    public static final String KEY_HTTP_CLIENT_ID = "client-id";

    @Resource
    @Qualifier("acSignatureInfoParserImpl")
    private SignatureService acSignatureInfoParserImpl;

    @Resource
    @Qualifier("messageEngine")
    private MessageEngine messageEngine;

    @Value("${default.result.struct.in.response.enable:true}")
    private String enableDefaultResultStructInResponse;

    @Value("${use.ac.format.result.when.gateway.exception:false}")
    private String useAcFormatResultWhenGatewayException;

    @Override
    public void doBusiness(GatewayRuntimeProcessContext gatewayRuntimeProcessContext) {
        MessageEnvelope resultMessage = gatewayRuntimeProcessContext.getResultMessage();

        MessageEnvelope messageResponseMessage = resultMessage.clone();
        Map<String, String> headers = messageResponseMessage.getHeaders();
        headers.put(KEY_HTTP_CLIENT_ID, gatewayRuntimeProcessContext.getRequesterClientId());

        if (null == gatewayRuntimeProcessContext.getBusinessException()
                && gatewayRuntimeProcessContext.isNeedStatus()) {
            String content = StringUtils.EMPTY;
            try {
                content = messageResponseMessage.getStrContent();
                if (StringUtils.isNotBlank(content)) {
                    JSONObject jsonResult = JSON.parseObject(content);
                    if (null == jsonResult.getJSONObject("result")) {
                        if (Boolean.parseBoolean(enableDefaultResultStructInResponse)) {
                            JSONObject result = CommonUtil
                                    .assembleAcResult(GatewayErrorCode.SUCCESS);
                            // get switch
                            if (Boolean.parseBoolean(useAcFormatResultWhenGatewayException)) {
                                // if correct ac format result switch is opened
                                result = CommonUtil
                                        .assembleAcResultWhenException(GatewayErrorCode.SUCCESS);
                            }
                            jsonResult.put("result", result);
                        }
                    }
                    messageResponseMessage.setContent(jsonResult.toJSONString());
                }
            } catch (Throwable e) {
                throw new CommonException(GatewayErrorCode.UNKNOWN_EXCEPTION, content, e);
            }
        }
        messageResponseMessage.putExtMap(KEY_HTTP_CLIENT_ID,
                gatewayRuntimeProcessContext.getRequesterClientId());
        messageResponseMessage.putExtMap(KEY_HTTP_METHOD,
                gatewayRuntimeProcessContext.getRequesterHttpMethod());
        messageResponseMessage.putExtMap(KEY_HTTP_REQUEST_URI,
                gatewayRuntimeProcessContext.getRequesterUri());
        messageResponseMessage
                .setSignatureCertCode(gatewayRuntimeProcessContext.getRequesterCertCode());

        InterfaceConfig interfaceConfig = gatewayRuntimeProcessContext
                .getRequesterInterfaceConfig();
        if (null == interfaceConfig) {
            // 对于通过MessageService.send发送的SPI，走到这里就结束了。
            assemble(messageResponseMessage);
            gatewayRuntimeProcessContext.setResponseMessage(messageResponseMessage);
            return;
        }
        String requesterInterfaceId = gatewayRuntimeProcessContext.getRequesterInterfaceConfig()
                .getInterfaceId();
        String beanName = GroovyScriptCache.getBeanName(requesterInterfaceId, RoleType.SENDER,
                gatewayRuntimeProcessContext.getTransactionType());
        if (StringUtils.isBlank(beanName)) {
            assemble(messageResponseMessage);
        } else {
            UnifyMsg unifyMsg = new UnifyMsg();
            unifyMsg.addField("code", "code");
            unifyMsg.addField("message", "status");
            unifyMsg.addField("certCode", gatewayRuntimeProcessContext.getRequesterCertCode());
            unifyMsg.addField("content", messageResponseMessage.getStrContent());

            unifyMsg.addField(KEY_HTTP_REQUEST_URI, gatewayRuntimeProcessContext.getRequesterUri());
            unifyMsg.addField(KEY_HTTP_METHOD, gatewayRuntimeProcessContext.getRequesterHttpMethod());
            unifyMsg.addField(KEY_HTTP_CLIENT_ID, messageResponseMessage.pullExtMapValue(KEY_HTTP_CLIENT_ID));
            unifyMsg.addField(KEY_HTTP_RESPONSE_TIME,
                    DateFormatUtils.format(new Date(), DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.getPattern()));

            messageResponseMessage = messageEngine.assemble(requesterInterfaceId, RoleType.SENDER,
                    gatewayRuntimeProcessContext, gatewayRuntimeProcessContext.getTransactionType(),
                    unifyMsg);

        }
        gatewayRuntimeProcessContext.setResponseMessage(messageResponseMessage);
    }

    @Override
    @Resource
    @Qualifier("terminalServiceAction")
    public void setNextAction(BusinessAction terminalServiceAction) {
        this.nextAction = terminalServiceAction;
    }

    private MessageEnvelope assemble(MessageEnvelope responseMessage) {
        String content = String.valueOf(responseMessage.getContent());
        Map<String, String> headers = responseMessage.getHeaders();

        String clientId = responseMessage.pullExtMapValue(KEY_HTTP_CLIENT_ID);
        String certCode = responseMessage.getSignatureCertCode();
        String responseTime = DateUtil.getISODatetimeString(new Date());

        // TODO 20191231, TY, Signature content need to have http method and the uri
        String httpMethod = responseMessage.pullExtMapValue(KEY_HTTP_METHOD);
        String httpUri = responseMessage.pullExtMapValue(KEY_HTTP_REQUEST_URI);
        SignatureRequest signatureRequest = new SignatureRequest();
        signatureRequest.setHttpMethod(httpMethod);
        signatureRequest.setHttpUri(httpUri);
        signatureRequest.setClientId(clientId);
        signatureRequest.setResponseTime(responseTime);
        signatureRequest.setMessage(content);
        signatureRequest.setSecurityStrategyCode(certCode);
        String signature = acSignatureInfoParserImpl.sign(responseMessage, signatureRequest);
        // set the header info
        headers.put(KEY_SIGNATURE, signature);
        headers.put(KEY_HTTP_CLIENT_ID, clientId);
        headers.put(KEY_HTTP_RESPONSE_TIME, responseTime);
        return responseMessage;
    }
}
