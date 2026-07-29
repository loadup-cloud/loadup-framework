package io.github.loadup.gateway.facade.context;

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

    public GatewayContext(GatewayRequest request, HttpServletRequest originalRequest, GatewayResponse response, HttpServletResponse originalResponse, RouteConfig route, Map<String, Object> attributes, Throwable exception) {
        this.request = request;
        this.originalRequest = originalRequest;
        this.response = response;
        this.originalResponse = originalResponse;
        this.route = route;
        this.attributes = attributes;
        this.exception = exception;
    }

    public GatewayContext() {
    }

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

    @Override
    public int hashCode() {
        return java.util.Objects.hash(request, originalRequest, response, originalResponse, route, attributes, exception);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GatewayContext other = (GatewayContext) o;
        if (!java.util.Objects.equals(request, other.request)) return false;
        if (!java.util.Objects.equals(originalRequest, other.originalRequest)) return false;
        if (!java.util.Objects.equals(response, other.response)) return false;
        if (!java.util.Objects.equals(originalResponse, other.originalResponse)) return false;
        if (!java.util.Objects.equals(route, other.route)) return false;
        if (!java.util.Objects.equals(attributes, other.attributes)) return false;
        if (!java.util.Objects.equals(exception, other.exception)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "GatewayContext(" + "request=" + request + ", " + "originalRequest=" + originalRequest + ", " + "response=" + response + ", " + "originalResponse=" + originalResponse + ", " + "route=" + route + ", " + "attributes=" + attributes + ", " + "exception=" + exception + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private GatewayRequest request;
        private HttpServletRequest originalRequest;
        private GatewayResponse response;
        private HttpServletResponse originalResponse;
        private RouteConfig route;
        private Map<String, Object> attributes = new ConcurrentHashMap<>();
        private Throwable exception;

        public Builder request(GatewayRequest request) {
            this.request = request;
            return this;
        }

        public Builder originalRequest(HttpServletRequest originalRequest) {
            this.originalRequest = originalRequest;
            return this;
        }

        public Builder response(GatewayResponse response) {
            this.response = response;
            return this;
        }

        public Builder originalResponse(HttpServletResponse originalResponse) {
            this.originalResponse = originalResponse;
            return this;
        }

        public Builder route(RouteConfig route) {
            this.route = route;
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder exception(Throwable exception) {
            this.exception = exception;
            return this;
        }

        public GatewayContext build() {
            return new GatewayContext(this.request, this.originalRequest, this.response, this.originalResponse, this.route, this.attributes, this.exception);
        }
    }
}
