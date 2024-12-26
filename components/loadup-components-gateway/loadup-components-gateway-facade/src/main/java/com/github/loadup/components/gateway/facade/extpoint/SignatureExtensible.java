package com.github.loadup.components.gateway.facade.extpoint;

import com.alibaba.cola.extension.Extension;

import java.util.Map;

/**
 *
 */
@Extension
public interface SignatureExtensible {

    /**
     * key used in gateway runtime velocity util classes cache. Each should be unique.
     */
    String getKey();

    /**
     * 3rd party signature method used by extension loader
     */
    String thirdPartySign(String bizCode, String certKey, String content,
                          Map<String, String> message);

    /**
     * 3rd party signature verify method used by extension loader
     */
    boolean thirdPartyVerify(String certKey, String srcContent, String signature,
                             String signedContent, Map<String, String> message);

}