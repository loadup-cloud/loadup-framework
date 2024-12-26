package com.github.loadup.components.gateway.core.ctrl.action.atom;

import com.alibaba.fastjson2.JSONObject;
import com.github.loadup.components.gateway.common.exception.ErrorCode;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.common.util.CommonUtil;
import com.github.loadup.components.gateway.core.common.enums.MessageFormat;
import com.github.loadup.components.gateway.core.ctrl.action.AbstractBusinessAction;
import com.github.loadup.components.gateway.core.ctrl.action.BusinessAction;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * <p>
 * ReceiveParseAction.java
 * </p>
 */
@Component("exceptionAssembleAction")
public class ExceptionAssembleAction extends AbstractBusinessAction {

    private static final Logger logger = LoggerFactory.getLogger("COMMON-ERROR-LOGGER");

    @Value("${use.ac.format.result.when.gateway.exception:false}")
    private String useAcFormatResultWhenGatewayException;

    @Override
    public void doBusiness(GatewayRuntimeProcessContext gatewayRuntimeProcessContext) {
        GatewayException gatewayException = gatewayRuntimeProcessContext.getBusinessException();
        LogUtil.error(logger, gatewayException, "error message is ");
        ErrorCode errorCode = gatewayException.getErrorCode();

        JSONObject jsonResult = buildResult(errorCode, gatewayException);

        String content = jsonResult.toJSONString();
        MessageEnvelope resultMsg = new MessageEnvelope(MessageFormat.TEXT, content);
        gatewayRuntimeProcessContext.setResultMessage(resultMsg);

    }

    /**
     * build result body
     */
    private JSONObject buildResult(ErrorCode errorCode, GatewayException gatewayException) {
        // get switch
        if (Boolean.parseBoolean(useAcFormatResultWhenGatewayException)) {
            // correct ac format result
            JSONObject jsonStatus = CommonUtil.assembleAcResultWhenException(errorCode);
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("result", jsonStatus);
            return jsonResult;
        }

        // gateway format result
        JSONObject jsonStatus = CommonUtil.assembleAcResult(errorCode);

        JSONObject jsonResult = new JSONObject();
        jsonResult.put("result", jsonStatus);
        //if(!StringUtils.equals(errorCode.getMessage(), gatewayliteException.getMessage())) {
        jsonResult.put("errorMessage", gatewayException.getMessage());
        //}

        return jsonResult;
    }

    @Override
    @Resource
    @Qualifier("limitReleaseAction")
    public void setNextAction(BusinessAction responseAssembleAction) {
        this.nextAction = responseAssembleAction;
    }

}