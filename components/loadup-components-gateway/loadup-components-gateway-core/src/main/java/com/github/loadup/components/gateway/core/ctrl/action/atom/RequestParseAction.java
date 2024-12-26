package com.github.loadup.components.gateway.core.ctrl.action.atom;

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
        messageEnvelope
                .setNeedVerifySignature(needVerifySignature(transactionType, requesterCertCode));

        UnifyMsg unifyMessage = null;

        String requesterInterfaceId = context.getRequesterInterfaceConfig() == null
                ? StringUtils.EMPTY
                : context.getRequesterInterfaceConfig().getInterfaceId();

        unifyMessage = messageEngine.parse(requesterInterfaceId, RoleType.SENDER, context,
                messageEnvelope);

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