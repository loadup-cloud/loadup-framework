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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Gateway request model
 */
public class GatewayRequest {

    /**
     * Request ID
     */
    private String requestId;

    /**
     * Request path
     */
    private String path;

    /**
     * HTTP method
     */
    private String method;

    /**
     * Request headers
     */
    private Map<String, String> headers;

    /**
     * Query parameters
     */
    private Map<String, List<String>> queryParameters;

    /**
     * Path parameters
     */
    private Map<String, String> pathParameters;

    /**
     * Request body
     */
    private String body;

    /**
     * Content type
     */
    private String contentType;

    /**
     * Client IP
     */
    private String clientIp;

    /**
     * User agent
     */
    private String userAgent;

    /**
     * Request time
     */
    private LocalDateTime requestTime;

    /**
     * Extension attributes
     */
    private Map<String, Object> attributes;

    public GatewayRequest(
            String requestId,
            String path,
            String method,
            Map<String, String> headers,
            Map<String, List<String>> queryParameters,
            Map<String, String> pathParameters,
            String body,
            String contentType,
            String clientIp,
            String userAgent,
            LocalDateTime requestTime,
            Map<String, Object> attributes) {
        this.requestId = requestId;
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.queryParameters = queryParameters;
        this.pathParameters = pathParameters;
        this.body = body;
        this.contentType = contentType;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.requestTime = requestTime;
        this.attributes = attributes;
    }

    public GatewayRequest() {}

    public String getRequestId() {
        return this.requestId;
    }

    public String getPath() {
        return this.path;
    }

    public String getMethod() {
        return this.method;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public Map<String, List<String>> getQueryParameters() {
        return this.queryParameters;
    }

    public Map<String, String> getPathParameters() {
        return this.pathParameters;
    }

    public String getBody() {
        return this.body;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getClientIp() {
        return this.clientIp;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public LocalDateTime getRequestTime() {
        return this.requestTime;
    }

    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setQueryParameters(Map<String, List<String>> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public void setPathParameters(Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String requestId;
        private String path;
        private String method;
        private Map<String, String> headers;
        private Map<String, List<String>> queryParameters;
        private Map<String, String> pathParameters;
        private String body;
        private String contentType;
        private String clientIp;
        private String userAgent;
        private LocalDateTime requestTime;
        private Map<String, Object> attributes;

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder queryParameters(Map<String, List<String>> queryParameters) {
            this.queryParameters = queryParameters;
            return this;
        }

        public Builder pathParameters(Map<String, String> pathParameters) {
            this.pathParameters = pathParameters;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder requestTime(LocalDateTime requestTime) {
            this.requestTime = requestTime;
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public GatewayRequest build() {
            return new GatewayRequest(
                    this.requestId,
                    this.path,
                    this.method,
                    this.headers,
                    this.queryParameters,
                    this.pathParameters,
                    this.body,
                    this.contentType,
                    this.clientIp,
                    this.userAgent,
                    this.requestTime,
                    this.attributes);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
