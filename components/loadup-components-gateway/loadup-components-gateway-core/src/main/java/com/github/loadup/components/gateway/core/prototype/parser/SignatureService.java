package com.github.loadup.components.gateway.core.prototype.parser;

import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.core.prototype.model.SignatureRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>
 * SignatureInfoParser.java
 *
 * </p>
 */
public interface SignatureService {

    /**
     * assemble signature info with httpRequest
     */
    String getSignatureInfo(HttpServletRequest httpServletRequest, MessageEnvelope envelope);

    /**
     * assemble signature info with httpRequest
     */
    String getOriginSignatureInfo(HttpServletRequest httpServletRequest, MessageEnvelope envelope);

    /**
     * get signature content
     */
    String sign(MessageEnvelope messageEnvelope, SignatureRequest signatureRequest);

}