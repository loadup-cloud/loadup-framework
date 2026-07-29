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
import java.util.Map;

/**
 * Gateway response model
 */
public class GatewayResponse {

    /**
     * Request ID
     */
    private String requestId;

    /**
     * Status code
     */
    private int statusCode;

    /**
     * Response headers
     */
    private Map<String, String> headers;

    /**
     * Response body
     */
    private String body;

    /**
     * Content type
     */
    private String contentType;

    /**
     * Response time
     */
    private LocalDateTime responseTime;

    /**
     * Processing time in milliseconds
     */
    private long processingTime;

    /**
     * Error message
     */
    private String errorMessage;

    /**
     * Extension attributes
     */
    private Map<String, Object> attributes;

    public GatewayResponse(
            String requestId,
            int statusCode,
            Map<String, String> headers,
            String body,
            String contentType,
            LocalDateTime responseTime,
            long processingTime,
            String errorMessage,
            Map<String, Object> attributes) {
        this.requestId = requestId;
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
        this.contentType = contentType;
        this.responseTime = responseTime;
        this.processingTime = processingTime;
        this.errorMessage = errorMessage;
        this.attributes = attributes;
    }

    public GatewayResponse() {}

    public String getRequestId() {
        return this.requestId;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public String getBody() {
        return this.body;
    }

    public String getContentType() {
        return this.contentType;
    }

    public LocalDateTime getResponseTime() {
        return this.responseTime;
    }

    public long getProcessingTime() {
        return this.processingTime;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setResponseTime(LocalDateTime responseTime) {
        this.responseTime = responseTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                requestId,
                statusCode,
                headers,
                body,
                contentType,
                responseTime,
                processingTime,
                errorMessage,
                attributes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GatewayResponse other = (GatewayResponse) o;
        if (!java.util.Objects.equals(requestId, other.requestId)) return false;
        if (!java.util.Objects.equals(statusCode, other.statusCode)) return false;
        if (!java.util.Objects.equals(headers, other.headers)) return false;
        if (!java.util.Objects.equals(body, other.body)) return false;
        if (!java.util.Objects.equals(contentType, other.contentType)) return false;
        if (!java.util.Objects.equals(responseTime, other.responseTime)) return false;
        if (!java.util.Objects.equals(processingTime, other.processingTime)) return false;
        if (!java.util.Objects.equals(errorMessage, other.errorMessage)) return false;
        if (!java.util.Objects.equals(attributes, other.attributes)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "GatewayResponse(" + "requestId=" + requestId + ", " + "statusCode=" + statusCode + ", " + "headers="
                + headers + ", " + "body=" + body + ", " + "contentType=" + contentType + ", " + "responseTime="
                + responseTime + ", " + "processingTime=" + processingTime + ", " + "errorMessage=" + errorMessage
                + ", " + "attributes=" + attributes + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String requestId;
        private int statusCode;
        private Map<String, String> headers;
        private String body;
        private String contentType;
        private LocalDateTime responseTime;
        private long processingTime;
        private String errorMessage;
        private Map<String, Object> attributes;

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
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

        public Builder responseTime(LocalDateTime responseTime) {
            this.responseTime = responseTime;
            return this;
        }

        public Builder processingTime(long processingTime) {
            this.processingTime = processingTime;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public GatewayResponse build() {
            return new GatewayResponse(
                    this.requestId,
                    this.statusCode,
                    this.headers,
                    this.body,
                    this.contentType,
                    this.responseTime,
                    this.processingTime,
                    this.errorMessage,
                    this.attributes);
        }
    }
}
