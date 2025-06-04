/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.facade.model;

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
 * <p>
 * AuthRequest.java
 * </p>
 */
public class AuthRequest {

    private String accessToken;

    private String authClientId;

    private String referenceClientId;

    private String scope;

    private String resourceId;

    private String resourceOwnerId;

    private String extendInfo;

    /**
     * Gets get access token.
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets set access token.
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Gets get auth client id.
     */
    public String getAuthClientId() {
        return authClientId;
    }

    /**
     * Sets set auth client id.
     */
    public void setAuthClientId(String authClientId) {
        this.authClientId = authClientId;
    }

    /**
     * Gets get reference client id.
     */
    public String getReferenceClientId() {
        return referenceClientId;
    }

    /**
     * Sets set reference client id.
     */
    public void setReferenceClientId(String referenceClientId) {
        this.referenceClientId = referenceClientId;
    }

    /**
     * Gets get scope.
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets set scope.
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Gets get resource id.
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Sets set resource id.
     */
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * Gets get resource owner id.
     */
    public String getResourceOwnerId() {
        return resourceOwnerId;
    }

    /**
     * Sets set resource owner id.
     */
    public void setResourceOwnerId(String resourceOwnerId) {
        this.resourceOwnerId = resourceOwnerId;
    }

    /**
     * Gets get extend info.
     */
    public String getExtendInfo() {
        return extendInfo;
    }

    /**
     * Sets set extend info.
     */
    public void setExtendInfo(String extendInfo) {
        this.extendInfo = extendInfo;
    }
}
