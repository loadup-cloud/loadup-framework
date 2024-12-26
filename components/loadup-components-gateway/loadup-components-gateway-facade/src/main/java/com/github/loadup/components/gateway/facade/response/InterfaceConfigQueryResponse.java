package com.github.loadup.components.gateway.facade.response;

import java.util.List;

/**
 *
 */
public class InterfaceConfigQueryResponse extends BaseResponse {

    private List<InterfaceConfigInnerResponse> interfaceConfigList;

    /**
     * Getter method for property <tt>interfaceConfigList</tt>.
     */
    public List<InterfaceConfigInnerResponse> getInterfaceConfigList() {
        return interfaceConfigList;
    }

    /**
     * Setter method for property <tt>interfaceConfigList</tt>.
     */
    public void setInterfaceConfigList(List<InterfaceConfigInnerResponse> interfaceConfigList) {
        this.interfaceConfigList = interfaceConfigList;
    }
}