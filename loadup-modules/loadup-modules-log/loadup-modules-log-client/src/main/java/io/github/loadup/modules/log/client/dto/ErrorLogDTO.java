package io.github.loadup.modules.log.client.dto;

/*-
 * #%L
 * Loadup Modules Log Client
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

/**
 * Error log DTO.
 */
public class ErrorLogDTO {

    private String id;
    private String userId;
    /**
     * BUSINESS / SYSTEM / THIRD_PARTY
     */
    private String errorType;

    private String errorCode;
    private String errorMessage;
    private String stackTrace;
    private String requestUrl;
    private String requestMethod;
    private String requestParams;
    private String ip;
    private java.time.LocalDateTime errorTime;

    public ErrorLogDTO(
            String id,
            String userId,
            String errorType,
            String errorCode,
            String errorMessage,
            String stackTrace,
            String requestUrl,
            String requestMethod,
            String requestParams,
            String ip,
            java.time.LocalDateTime errorTime) {
        this.id = id;
        this.userId = userId;
        this.errorType = errorType;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
        this.requestUrl = requestUrl;
        this.requestMethod = requestMethod;
        this.requestParams = requestParams;
        this.ip = ip;
        this.errorTime = errorTime;
    }

    public ErrorLogDTO() {}

    public String getId() {
        return this.id;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getErrorType() {
        return this.errorType;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getStackTrace() {
        return this.stackTrace;
    }

    public String getRequestUrl() {
        return this.requestUrl;
    }

    public String getRequestMethod() {
        return this.requestMethod;
    }

    public String getRequestParams() {
        return this.requestParams;
    }

    public String getIp() {
        return this.ip;
    }

    public java.time.LocalDateTime getErrorTime() {
        return this.errorTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setErrorTime(java.time.LocalDateTime errorTime) {
        this.errorTime = errorTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String userId;
        private String errorType;
        private String errorCode;
        private String errorMessage;
        private String stackTrace;
        private String requestUrl;
        private String requestMethod;
        private String requestParams;
        private String ip;
        private java.time.LocalDateTime errorTime;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder errorType(String errorType) {
            this.errorType = errorType;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder stackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
            return this;
        }

        public Builder requestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
            return this;
        }

        public Builder requestMethod(String requestMethod) {
            this.requestMethod = requestMethod;
            return this;
        }

        public Builder requestParams(String requestParams) {
            this.requestParams = requestParams;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder errorTime(java.time.LocalDateTime errorTime) {
            this.errorTime = errorTime;
            return this;
        }

        public ErrorLogDTO build() {
            return new ErrorLogDTO(
                    this.id,
                    this.userId,
                    this.errorType,
                    this.errorCode,
                    this.errorMessage,
                    this.stackTrace,
                    this.requestUrl,
                    this.requestMethod,
                    this.requestParams,
                    this.ip,
                    this.errorTime);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
