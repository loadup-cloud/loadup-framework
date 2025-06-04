/* Copyright (C) LoadUp Cloud 2022-2025 */
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

import com.github.loadup.components.gateway.common.constant.OauthConstants;
import com.github.loadup.components.gateway.common.util.PropertiesUtil;
import com.github.loadup.components.gateway.core.common.enums.InterfaceType;
import com.github.loadup.components.gateway.core.common.enums.RoleType;
import com.github.loadup.components.gateway.core.ctrl.action.AbstractBusinessAction;
import com.github.loadup.components.gateway.core.ctrl.action.BusinessAction;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.core.model.InterfaceConfig;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants;
import com.github.loadup.components.gateway.core.prototype.constant.SwitchConstants;
import com.github.loadup.components.gateway.facade.model.AuthRequest;
import com.github.loadup.components.gateway.message.base.api.MessageEngine;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * <p>
 * requestParseAction.java
 * </p>
 */
@Component("requestParseAction")
public class RequestParseAction extends AbstractBusinessAction {

    @Resource
    @Qualifier("messageEngine")
    private MessageEngine messageEngine;

    //    @Resource
    //    private OauthService  oauthService;

    @Override
    public void doBusiness(GatewayRuntimeProcessContext context) {
        MessageEnvelope messageEnvelope = context.getRequestMessage();

        String transactionType = context.getTransactionType();
        String requesterCertCode = context.getRequesterCertCode();
        messageEnvelope.setNeedVerifySignature(needVerifySignature(transactionType, requesterCertCode));

        UnifyMsg unifyMessage = null;

        String requesterInterfaceId = context.getRequesterInterfaceConfig() == null
                ? StringUtils.EMPTY
                : context.getRequesterInterfaceConfig().getInterfaceId();

        unifyMessage = messageEngine.parse(requesterInterfaceId, RoleType.SENDER, context, messageEnvelope);

        InterfaceConfig interfaceConfig = context.getRequesterInterfaceConfig();
        checkOauth(unifyMessage, interfaceConfig);

        unifyMessage.addField(ProcessConstants.KEY_HTTP_INTEGRATION_URL, context.getIntegratorUrl());
        MessageEnvelope messageResult = messageEnvelope.clone();
        messageResult.setContent(unifyMessage.toJSonStr());
        messageResult.setLogContent(unifyMessage.toJSonStr());
        context.setResultMessage(messageResult);
    }

    private void checkOauth(UnifyMsg unifyMessage, InterfaceConfig interfaceConfig) {
        boolean oauthResult = true;
        AuthRequest authRequest = new AuthRequest();
        authRequest.setAccessToken(unifyMessage.g(OauthConstants.ACCESS_TOKEN));
        authRequest.setAuthClientId(unifyMessage.g(OauthConstants.AUTH_CLIENT_ID));
        authRequest.setExtendInfo(unifyMessage.g(OauthConstants.EXTEND_INFO));
        authRequest.setReferenceClientId(unifyMessage.g(OauthConstants.REFERENCE_CLIENT_ID));
        authRequest.setResourceId(unifyMessage.g(OauthConstants.RESOURCE_ID));
        authRequest.setScope(unifyMessage.g(OauthConstants.SCOPE));
        authRequest.setResourceOwnerId(OauthConstants.RESOURCE_OWNER_ID);

        String authType = StringUtils.EMPTY;
        if (null != interfaceConfig) {
            authType = PropertiesUtil.getProperty(interfaceConfig.getProperties(), "AUTH_TYPE");
        }

        //        if (StringUtils.equals(authType, "OAUTH_CLIENT")) {
        //            oauthResult = oauthService.validateClient(authRequest);
        //            AssertUtil.isTrue(oauthResult, OauthErrorCode.INVALID_AUTH_CLIENT,
        //                OauthErrorCode.INVALID_AUTH_CLIENT.getMessage());
        //
        //        } else if (StringUtils.equals(authType, "OAUTH_TOKEN")) {
        //            oauthResult = oauthService.validateClient(authRequest);
        //            AssertUtil.isTrue(oauthResult, OauthErrorCode.INVALID_AUTH_CLIENT,
        //                OauthErrorCode.INVALID_AUTH_CLIENT.getMessage());
        //            oauthResult = oauthService.validateToke(authRequest);
        //            AssertUtil.isTrue(oauthResult, OauthErrorCode.INVALID_ACCESS_TOKEN,
        //                OauthErrorCode.INVALID_ACCESS_TOKEN.getMessage());
        //        }

    }

    @Override
    @Resource
    @Qualifier("requestAssembleAction")
    public void setNextAction(BusinessAction requestParseAction) {
        this.nextAction = requestParseAction;
    }

    /**
     * 判断是否需要进行验签
     */
    private boolean needVerifySignature(String transactionType, String certCode) {
        if (StringUtils.equals(transactionType, InterfaceType.SPI.getCode())
                || StringUtils.equals(certCode, SwitchConstants.OFF)) {
            return false;
        }
        return true;
    }
}
