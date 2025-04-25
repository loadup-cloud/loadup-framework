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
     * 
     */
    public String getInterfaceId() {
        return interfaceId;
    }

    /**
     * 
     */
    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    /**
     * 
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * 
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * 
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * 
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 
     */
    public Integer getPage() {
        return page;
    }

    /**
     * 
     */
    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     * 
     */
    public String getTntInstId() {
        return tntInstId;
    }

    /**
     * 
     */
    public void setTntInstId(String tntInstId) {
        this.tntInstId = tntInstId;
    }

    /**
     * 
     */
    public String getType() {
        return type;
    }

    /**
     * 
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 
     */
    public String getStatus() {
        return status;
    }

    /**
     * 
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * 
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }
}
