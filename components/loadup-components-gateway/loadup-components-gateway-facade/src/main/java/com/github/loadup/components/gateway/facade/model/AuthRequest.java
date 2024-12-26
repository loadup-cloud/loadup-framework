package com.github.loadup.components.gateway.facade.model;

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