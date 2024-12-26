package com.github.loadup.components.gateway.facade.response;

import java.util.List;

/**
 *
 */
public class CertConfigQueryResponse extends BaseResponse {

    private List<CertConfigInnerResponse> certConfigList;

    /**
     * Getter method for property <tt>certConfigList</tt>.
     */
    public List<CertConfigInnerResponse> getCertConfigList() {
        return certConfigList;
    }

    /**
     * Setter method for property <tt>certConfigList</tt>.
     */
    public void setCertConfigList(List<CertConfigInnerResponse> certConfigList) {
        this.certConfigList = certConfigList;
    }
}