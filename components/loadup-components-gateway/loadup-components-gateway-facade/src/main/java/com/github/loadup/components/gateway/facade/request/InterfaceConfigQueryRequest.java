package com.github.loadup.components.gateway.facade.request;

/*-
 * #%L
 * loadup-components-gateway-facade
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

/**
 *
 */
public class InterfaceConfigQueryRequest extends BaseRequest {
    /**
     * interfaceId
     */
    private String interfaceId;
    /**
     * clientId
     */
    private String clientId;
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
    private String tntInstId;
    /**
     * type
     */
    private String type;
    /**
     * status
     */
    private String status;
    /**
     * interfaceName
     */
    private String interfaceName;

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
