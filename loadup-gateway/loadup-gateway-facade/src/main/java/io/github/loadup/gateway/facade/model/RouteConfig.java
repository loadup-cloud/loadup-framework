package io.github.loadup.gateway.facade.model;

/*-
 * #%L
 * LoadUp Gateway Facade
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Map;

/**
 * Route configuration model (immutable)
 */
public class RouteConfig {

    /**
     * Route ID (auto-generated, based on path + method)
     */
    private String routeId;

    /**
     * Route name (auto-generated, based on path)
     */
    private String routeName;

    /**
     * Match path
     */
    private String path;

    /**
     * HTTP method
     */
    private String method;

    /**
     * Protocol type (HTTP/RPC/BEAN)
     */
    private String protocol;

    /**
     * Unified target configuration (original string)
     */
    private String target;

    /**
     * Target URL (used for HTTP/RPC)
     */
    private String targetUrl;

    /**
     * Target Bean name (used for BEAN protocol)
     */
    private String targetBean;

    /**
     * Target method name (used for BEAN protocol)
     */
    private String targetMethod;

    /**
     * Whether enabled
     */
    private boolean enabled;

    /**
     * Extended configuration (immutable copy)
     */
    private Map<String, Object> properties;

    /**
     * Parsed timeout (milliseconds)
     */
    private long parsedTimeout;

    /**
     * Parsed retry count
     */
    private int parsedRetryCount;

    /**
     * Parsed wrapResponse (null means use global configuration)
     */
    private Boolean parsedWrapResponse;

    /**
     * Security code for authentication/signing strategy (e.g. "OFF", "default", "hmac")
     */
    private String securityCode;

    /**
     * Route-level authorization expression (Spring Security SpEL or comma-separated
     * authority/permission list, e.g. "hasRole('ADMIN')" or "user:list,user:delete").
     * Evaluated after the security strategy when present.
     */
    private String authorize;

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getTargetBean() {
        return targetBean;
    }

    public void setTargetBean(String targetBean) {
        this.targetBean = targetBean;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public long getParsedTimeout() {
        return parsedTimeout;
    }

    public void setParsedTimeout(long parsedTimeout) {
        this.parsedTimeout = parsedTimeout;
    }

    public int getParsedRetryCount() {
        return parsedRetryCount;
    }

    public void setParsedRetryCount(int parsedRetryCount) {
        this.parsedRetryCount = parsedRetryCount;
    }

    public Boolean getParsedWrapResponse() {
        return parsedWrapResponse;
    }

    public void setParsedWrapResponse(Boolean parsedWrapResponse) {
        this.parsedWrapResponse = parsedWrapResponse;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public String getAuthorize() {
        return authorize;
    }

    public void setAuthorize(String authorize) {
        this.authorize = authorize;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    // Public read methods return parsed cached values (regular field getters already generated by
    // @Getter)
    public long getTimeout() {
        return this.parsedTimeout;
    }

    public int getRetryCount() {
        return this.parsedRetryCount;
    }

    public Boolean getWrapResponse() {
        return this.parsedWrapResponse;
    }
}
