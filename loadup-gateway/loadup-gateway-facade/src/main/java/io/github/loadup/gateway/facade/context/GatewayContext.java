package io.github.loadup.gateway.facade.context;

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

import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gateway Context to hold request lifecycle objects.
 */
public class GatewayContext {

    /**
     * The incoming request
     */
    private GatewayRequest request;

    /**
     * The original HttpServletRequest
     */
    private HttpServletRequest originalRequest;

    /**
     * The outgoing response
     */
    private GatewayResponse response;

    /**
     * The original HttpServletResponse
     */
    private HttpServletResponse originalResponse;

    /**
     * The matched route configuration
     */
    private RouteConfig route;

    /**
     * Context attributes for sharing data between components
     */
    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    /**
     * Exception occurred during processing
     */
    private Throwable exception;

    /**
     * Add an attribute
     *
     * @param key   key
     * @param value value
     */
    public void setAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new ConcurrentHashMap<>();
        }
        attributes.put(key, value);
    }

    /**
     * Get an attribute
     *
     * @param key key
     * @param <T> type
     * @return value
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        if (attributes == null) {
            return null;
        }
        return (T) attributes.get(key);
    }

    /**
     * Remove an attribute
     *
     * @param key key
     */
    public void removeAttribute(String key) {
        if (attributes != null) {
            attributes.remove(key);
        }
    }

    public GatewayContext(
            GatewayRequest request,
            HttpServletRequest originalRequest,
            GatewayResponse response,
            HttpServletResponse originalResponse,
            RouteConfig route,
            Map<String, Object> attributes,
            Throwable exception) {
        this.request = request;
        this.originalRequest = originalRequest;
        this.response = response;
        this.originalResponse = originalResponse;
        this.route = route;
        this.attributes = attributes;
        this.exception = exception;
    }

    public GatewayContext() {}

    public GatewayRequest getRequest() {
        return this.request;
    }

    public HttpServletRequest getOriginalRequest() {
        return this.originalRequest;
    }

    public GatewayResponse getResponse() {
        return this.response;
    }

    public HttpServletResponse getOriginalResponse() {
        return this.originalResponse;
    }

    public RouteConfig getRoute() {
        return this.route;
    }

    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public Throwable getException() {
        return this.exception;
    }

    public void setRequest(GatewayRequest request) {
        this.request = request;
    }

    public void setOriginalRequest(HttpServletRequest originalRequest) {
        this.originalRequest = originalRequest;
    }

    public void setResponse(GatewayResponse response) {
        this.response = response;
    }

    public void setOriginalResponse(HttpServletResponse originalResponse) {
        this.originalResponse = originalResponse;
    }

    public void setRoute(RouteConfig route) {
        this.route = route;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }
}
