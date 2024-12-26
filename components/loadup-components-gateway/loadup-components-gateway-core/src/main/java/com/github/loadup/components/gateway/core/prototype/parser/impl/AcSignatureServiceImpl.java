package com.github.loadup.components.gateway.core.prototype.parser.impl;

import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.core.prototype.model.SignatureRequest;
import com.github.loadup.components.gateway.core.prototype.parser.SignatureService;
import com.github.loadup.components.gateway.core.prototype.util.HttpToolUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.github.loadup.components.gateway.core.prototype.constant.ProcessConstants.*;

/**
 * <p>
 * SignatureInfoParserImpl.java
 * </p>
 */
@Component("acSignatureInfoParserImpl")
public class AcSignatureServiceImpl implements SignatureService {
    private static Logger logger = LoggerFactory.getLogger(AcSignatureServiceImpl.class);

    private static final String SIGNATURE = "signature";

    @Value("${gatewaylite.signature.private.security.code:}")
    private String privateKey;

    @Override
    public String getSignatureInfo(HttpServletRequest httpServletRequest, MessageEnvelope envelope) {
        return httpServletRequest.getHeader(KEY_SIGNATURE);
    }

    @Override
    public String getOriginSignatureInfo(HttpServletRequest httpServletRequest, MessageEnvelope envelope) {
        String httpMethod = httpServletRequest.getMethod();
        String httpUri = httpServletRequest.getRequestURI();
        String clientId = HttpToolUtil.getHeaderFromHttpRequest(httpServletRequest, KEY_HTTP_CLIENT_ID);
        String requestTime = HttpToolUtil.getHeaderFromHttpRequest(httpServletRequest, KEY_HTTP_REQUEST_TIME);

        SignatureRequest signatureRequest = new SignatureRequest();
        signatureRequest.setClientId(clientId);
        signatureRequest.setHttpMethod(httpMethod);
        signatureRequest.setHttpUri(httpUri);
        signatureRequest.setRequestTime(requestTime);
        signatureRequest.setMessage(String.valueOf(envelope.getContent()));
        return signatureRequest.getVerifyRawStr();
    }

    @Override
    public String sign(MessageEnvelope messageEnvelope, SignatureRequest signatureRequest) {
        //        String clientId = signatureRequest.getClientId();
        //        String certContent = rsaSignatureUtil.getPrivateKeyContent(signatureRequest.getSecurityStrategyCode(), clientId);
        //        String signature = "";
        //        if (StringUtils.isBlank(certContent)) {
        //            return "algorithm=RSA256" + ",keyVersion=2" + ", signature=testing_signature";
        //        }
        //        try {
        //            LogUtil.debug(logger,
        //                    "=======sign raw str is " + signatureRequest.getSignRawStr() + "; certContent is" + certContent + ";
        //                    clientid is " + clientId);
        //            signature = rsaSignatureUtil.sign(signatureRequest.getSignRawStr(), certContent);
        //        } catch (Exception e) {
        //            LogUtil.error(logger, "generate signature error .", e);
        //        }
        //        return "algorithm=RSA256" + ",keyVersion=2" + ", signature=" + signature;
        return "";
    }

}