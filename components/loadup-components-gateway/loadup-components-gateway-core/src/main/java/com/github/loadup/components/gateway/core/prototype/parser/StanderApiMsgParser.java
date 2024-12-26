package com.github.loadup.components.gateway.core.prototype.parser;

import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.message.script.parser.MessageParser;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

import static com.github.loadup.components.gateway.core.common.GatewayliteErrorCode.INVALID_SIGNATURE;
import static com.github.loadup.components.gateway.core.common.GatewayliteErrorCode.PARAM_ILLEGAL;
import static com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants.KEY_PARSE_RESULT;
import static com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants.KEY_REQUEST_BODY;

/**
 * Customer Parser Template
 * 1. provide the defalut method
 * a. parseWithJson(String str):
 * parse the string to json format, and let you can get the data from the json object
 * b. putResult(Map<String, String> resultMap, String resultKey, String resultValue)
 * help you to input the result, the format is key-value.
 *
 * @date Nov 16, 2019 10:20:23 AM
 */
@Component("standerApiMsgParser")
public class StanderApiMsgParser implements MessageParser {
    public static final String KEY_HTTP_CLIENT_ID = "client-id";

    private final Logger logger = LoggerFactory.getLogger(StanderApiMsgParser.class);

    @Override
    public UnifyMsg parse(MessageEnvelope messageEnvelope) {
        UnifyMsg mesResult = new UnifyMsg();
        boolean verifySignatureSucceed = true;

        //1. verify the parameter
        String message = String.valueOf(messageEnvelope.getContent());
        Map<String, String> headerMap = messageEnvelope.getHeaders();

        if (verifyOpenApiParamsFail(headerMap)) {
            throw new GatewayException(PARAM_ILLEGAL);
        }
        String clientId = headerMap.get(KEY_HTTP_CLIENT_ID);
        ////2.2 进行签名验证
        if (messageEnvelope.isNeedVerifySignature()) {
            verifySignatureSucceed = getVerifySignResult(messageEnvelope.getSignatureCertCode(), clientId, messageEnvelope);
        }
        ////2.3 验证失败直接返回
        if (!verifySignatureSucceed) {
            throw new GatewayException(INVALID_SIGNATURE);
        }

        //3 返回最终结果
        Map<String, Object> jsonObject = new HashMap<>();
        try {
            jsonObject = JsonUtil.toJsonMap(message);
            mesResult.addMap(jsonObject);
        } catch (Exception e) {
            LogUtil.error(logger, e, "json parse fail");
            jsonObject.put(KEY_REQUEST_BODY, message);
        }
        //jsonObject.put("clientId", clientId);
        //jsonObject.put(KEY_HTTP_CLIENT_ID, headerMap.get(KEY_HTTP_CLIENT_ID));
        mesResult.addField(KEY_PARSE_RESULT, JsonUtil.toJSONString(jsonObject));
        mesResult.addField(KEY_HTTP_CLIENT_ID, headerMap.get(KEY_HTTP_CLIENT_ID));
        return mesResult;
    }

    private boolean getVerifySignResult(String requesterCertCode, String clientId, MessageEnvelope messageEnvelope) {
        boolean isVerifySignOk;
        try {
            isVerifySignOk = true;
        } catch (Exception e) {

            isVerifySignOk = false;
        }
        return isVerifySignOk;
    }

    /**
     * check the parameters
     */
    private boolean verifyOpenApiParamsFail(Map<String, String> extMap) {
        return CollectionUtils.isEmpty(extMap);
    }

}
