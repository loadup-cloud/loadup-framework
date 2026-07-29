package io.github.loadup.gateway.facade.model;

/*-
 * #%L
 * LoadUp Gateway Facade
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.gateway.facade.constants.GatewayConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

/**
 * Route configuration model (immutable)
 */
public class RouteConfig {

    /**
     * Route ID (auto-generated, based on path + method)
     */
    private  String routeId;

    /**
     * Route name (auto-generated, based on path)
     */
    private  String routeName;

    /**
     * Match path
     */
    private  String path;

    /**
     * HTTP method
     */
    private  String method;

    /**
     * Protocol type (HTTP/RPC/BEAN)
     */
    private  String protocol;

    /**
     * Unified target configuration (original string)
     */
    private  String target;

    /**
     * Target URL (used for HTTP/RPC)
     */
    private  String targetUrl;

    /**
     * Target Bean name (used for BEAN protocol)
     */
    private  String targetBean;

    /**
     * Target method name (used for BEAN protocol)
     */
    private  String targetMethod;

    /**
     * Request template script
     */
    private  String requestTemplate;

    /**
     * Response template script
     */
    private  String responseTemplate;

    /**
     * Whether enabled
     */
    private  boolean enabled;

    /**
     * Extended configuration (immutable copy)
     */
    private  Map<String, Object> properties;

    /**
     * Parsed timeout (milliseconds)
     */
    private  long parsedTimeout;

    /**
     * Parsed retry count
     */
    private  int parsedRetryCount;

    /**
     * Parsed wrapResponse (null means use global configuration)
     */
    private  Boolean parsedWrapResponse;

    /**
     * Security code for authentication/signing strategy (e.g. "OFF", "default", "hmac")
     */
    private  String securityCode;


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

    public String getRequestTemplate() {
        return requestTemplate;
    }

    public void setRequestTemplate(String requestTemplate) {
        this.requestTemplate = requestTemplate;
    }

    public String getResponseTemplate() {
        return responseTemplate;
    }

    public void setResponseTemplate(String responseTemplate) {
        this.responseTemplate = responseTemplate;
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

    // Internal static helper class and methods
    private static class TargetParseResult {
        private String protocol;
        private String targetUrl;
        private String targetBean;
        private String targetMethod;
    }

    private static TargetParseResult parseTarget(String target) {
        TargetParseResult r = new TargetParseResult();
        if (target == null || target.trim().isEmpty()) {
            return r;
        }

        if (startsWithIgnoreCase(target, GatewayConstants.Protocol.HTTP + "://")
            || startsWithIgnoreCase(target, GatewayConstants.Protocol.HTTP + "s://")) {
            r.protocol = GatewayConstants.Protocol.HTTP;
            r.targetUrl = target;
            return r;
        }

        if (startsWithIgnoreCase(target, GatewayConstants.Protocol.BEAN + "://")) {
            r.protocol = GatewayConstants.Protocol.BEAN;
            String beanTarget = target.substring(7); // remove "bean://"
            String[] parts = beanTarget.split(":");
            if (parts.length >= 1) {
                r.targetBean = parts[0];
            }
            if (parts.length >= 2) {
                r.targetMethod = parts[1];
            }
            return r;
        }

        if (startsWithIgnoreCase(target, GatewayConstants.Protocol.RPC + "://")) {
            r.protocol = GatewayConstants.Protocol.RPC;
            r.targetUrl = target.substring(6);
            return r;
        }

        return r;
    }

    private static class PropertiesParseResult {
        private long timeout = 30000L;
        private int retryCount = 3;
        private Boolean wrapResponse = null;
    }

    private static PropertiesParseResult parseProperties(Map<String, Object> properties) {
        PropertiesParseResult r = new PropertiesParseResult();
        if (properties == null || properties.isEmpty()) {
            return r;
        }

        Object timeout = properties.get(GatewayConstants.PropertyKeys.TIMEOUT);
        if (timeout instanceof Number) {
            r.timeout = ((Number) timeout).longValue();
        } else if (timeout instanceof String) {
            try {
                r.timeout = parseLong((String) timeout);
            } catch (NumberFormatException ignored) {
            }
        }

        Object retry = properties.get(GatewayConstants.PropertyKeys.RETRY_COUNT);
        if (retry instanceof Number) {
            r.retryCount = ((Number) retry).intValue();
        } else if (retry instanceof String) {
            try {
                r.retryCount = parseInt((String) retry);
            } catch (NumberFormatException ignored) {
            }
        }

        Object wrap = properties.get(GatewayConstants.PropertyKeys.WRAP_RESPONSE);
        if (wrap instanceof Boolean) {
            r.wrapResponse = (Boolean) wrap;
        } else if (wrap instanceof String) {
            r.wrapResponse = parseBoolean((String) wrap);
        }

        return r;
    }

    private static boolean startsWithIgnoreCase(String str, String prefix) {
        return str != null && str.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT));
    }

    private static String generateRouteId(String path, String method) {
        String combined = path + ":" + method;
        return "route-" + Math.abs(combined.hashCode());
    }

    private static String generateRouteName(String path, String method) {
        String name = path.replaceAll("^/", "")
            .replaceAll("/", " ")
            .replaceAll("-", " ")
            .trim();
        if (name.isEmpty()) {
            name = "root";
        }
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        return name + " (" + method + ")";
    }


}
