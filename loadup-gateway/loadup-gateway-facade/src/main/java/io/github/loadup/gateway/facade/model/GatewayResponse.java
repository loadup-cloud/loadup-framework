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

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
