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

import com.alibaba.fastjson2.JSONObject;
import com.github.loadup.commons.error.CommonException;
import com.github.loadup.commons.result.ResultCode;
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
        CommonException commonException = gatewayRuntimeProcessContext.getBusinessException();
        LogUtil.error(logger, commonException, "error message is ");
        ResultCode errorCode = commonException.getResultCode();

        JSONObject jsonResult = buildResult(errorCode, commonException);

        String content = jsonResult.toJSONString();
        MessageEnvelope resultMsg = new MessageEnvelope(MessageFormat.TEXT, content);
        gatewayRuntimeProcessContext.setResultMessage(resultMsg);
    }

    /**
     * build result body
     */
    private JSONObject buildResult(ResultCode errorCode, CommonException commonException) {
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
        // if(!StringUtils.equals(errorCode.getMessage(), gatewayliteException.getMessage())) {
        jsonResult.put("errorMessage", commonException.getMessage());
        // }

        return jsonResult;
    }

    @Override
    @Resource
    @Qualifier("limitReleaseAction")
    public void setNextAction(BusinessAction responseAssembleAction) {
        this.nextAction = responseAssembleAction;
    }
}
