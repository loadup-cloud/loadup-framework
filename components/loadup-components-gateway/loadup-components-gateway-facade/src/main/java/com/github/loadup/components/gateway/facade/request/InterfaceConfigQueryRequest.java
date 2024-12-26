package com.github.loadup.components.gateway.facade.request;

/**
 *
 */
public class InterfaceConfigQueryRequest extends BaseRequest {
    /**
     * interfaceId
     */
    private String  interfaceId;
    /**
     * clientId
     */
    private String  clientId;
    /**
     * pageSize
     */
    private Integer pageSize;
    /**
     * page
     */
    private Integer page;
    /**
     * tntInstId
     */
    private String  tntInstId;
    /**
     * type
     */
    private String  type;
    /**
     * status
     */
    private String  status;
    /**
     * interfaceName
     */
    private String  interfaceName;

    /**
     * Getter method for property <tt>interfaceId</tt>.
     */
    public String getInterfaceId() {
        return interfaceId;
    }

    /**
     * Setter method for property <tt>interfaceId</tt>.
     */
    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    /**
     * Getter method for property <tt>clientId</tt>.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Setter method for property <tt>clientId</tt>.
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Getter method for property <tt>pageSize</tt>.
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Setter method for property <tt>pageSize</tt>.
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Getter method for property <tt>page</tt>.
     */
    public Integer getPage() {
        return page;
    }

    /**
     * Setter method for property <tt>page</tt>.
     */
    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     * Getter method for property <tt>tntInstId</tt>.
     */
    public String getTntInstId() {
        return tntInstId;
    }

    /**
     * Setter method for property <tt>tntInstId</tt>.
     */
    public void setTntInstId(String tntInstId) {
        this.tntInstId = tntInstId;
    }

    /**
     * Getter method for property <tt>type</tt>.
     */
    public String getType() {
        return type;
    }

    /**
     * Setter method for property <tt>type</tt>.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter method for property <tt>status</tt>.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Setter method for property <tt>status</tt>.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Getter method for property <tt>interfaceName</tt>.
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * Setter method for property <tt>interfaceName</tt>.
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }
}