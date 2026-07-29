package io.github.loadup.modules.log.domain.model;

/*-
 * #%L
 * Loadup Modules Log Domain
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

/**
 * Domain model for operation log.
 *
 * <p>Pure POJO — no persistence framework annotations.
 */
public class OperationLog {

    private String id;
    private String traceId;
    private String userId;
    private String username;
    private String module;
    private String operationType;
    private String description;
    private String method;
    private String requestMethod;
    private String requestUrl;
    private String requestParams;
    private String responseResult;
    private Long duration;
    private Boolean success;
    private String errorMessage;
    private String ip;
    private String userAgent;
    private LocalDateTime operationTime;
    private LocalDateTime createdAt;

    public OperationLog(String id, String traceId, String userId, String username, String module, String operationType, String description, String method, String requestMethod, String requestUrl, String requestParams, String responseResult, Long duration, Boolean success, String errorMessage, String ip, String userAgent, LocalDateTime operationTime, LocalDateTime createdAt) {
        this.id = id;
        this.traceId = traceId;
        this.userId = userId;
        this.username = username;
        this.module = module;
        this.operationType = operationType;
        this.description = description;
        this.method = method;
        this.requestMethod = requestMethod;
        this.requestUrl = requestUrl;
        this.requestParams = requestParams;
        this.responseResult = responseResult;
        this.duration = duration;
        this.success = success;
        this.errorMessage = errorMessage;
        this.ip = ip;
        this.userAgent = userAgent;
        this.operationTime = operationTime;
        this.createdAt = createdAt;
    }

    public OperationLog() {
    }

    public String getId() {
        return this.id;
    }

    public String getTraceId() {
        return this.traceId;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return this.username;
    }

    public String getModule() {
        return this.module;
    }

    public String getOperationType() {
        return this.operationType;
    }

    public String getDescription() {
        return this.description;
    }

    public String getMethod() {
        return this.method;
    }

    public String getRequestMethod() {
        return this.requestMethod;
    }

    public String getRequestUrl() {
        return this.requestUrl;
    }

    public String getRequestParams() {
        return this.requestParams;
    }

    public String getResponseResult() {
        return this.responseResult;
    }

    public Long getDuration() {
        return this.duration;
    }

    public Boolean isSuccess() {
        return this.success;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getIp() {
        return this.ip;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public LocalDateTime getOperationTime() {
        return this.operationTime;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public void setResponseResult(String responseResult) {
        this.responseResult = responseResult;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, traceId, userId, username, module, operationType, description, method, requestMethod, requestUrl, requestParams, responseResult, duration, success, errorMessage, ip, userAgent, operationTime, createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationLog other = (OperationLog) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(traceId, other.traceId)) return false;
        if (!java.util.Objects.equals(userId, other.userId)) return false;
        if (!java.util.Objects.equals(username, other.username)) return false;
        if (!java.util.Objects.equals(module, other.module)) return false;
        if (!java.util.Objects.equals(operationType, other.operationType)) return false;
        if (!java.util.Objects.equals(description, other.description)) return false;
        if (!java.util.Objects.equals(method, other.method)) return false;
        if (!java.util.Objects.equals(requestMethod, other.requestMethod)) return false;
        if (!java.util.Objects.equals(requestUrl, other.requestUrl)) return false;
        if (!java.util.Objects.equals(requestParams, other.requestParams)) return false;
        if (!java.util.Objects.equals(responseResult, other.responseResult)) return false;
        if (!java.util.Objects.equals(duration, other.duration)) return false;
        if (!java.util.Objects.equals(success, other.success)) return false;
        if (!java.util.Objects.equals(errorMessage, other.errorMessage)) return false;
        if (!java.util.Objects.equals(ip, other.ip)) return false;
        if (!java.util.Objects.equals(userAgent, other.userAgent)) return false;
        if (!java.util.Objects.equals(operationTime, other.operationTime)) return false;
        if (!java.util.Objects.equals(createdAt, other.createdAt)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "OperationLog(" + "id=" + id + ", " + "traceId=" + traceId + ", " + "userId=" + userId + ", " + "username=" + username + ", " + "module=" + module + ", " + "operationType=" + operationType + ", " + "description=" + description + ", " + "method=" + method + ", " + "requestMethod=" + requestMethod + ", " + "requestUrl=" + requestUrl + ", " + "requestParams=" + requestParams + ", " + "responseResult=" + responseResult + ", " + "duration=" + duration + ", " + "success=" + success + ", " + "errorMessage=" + errorMessage + ", " + "ip=" + ip + ", " + "userAgent=" + userAgent + ", " + "operationTime=" + operationTime + ", " + "createdAt=" + createdAt + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String traceId;
        private String userId;
        private String username;
        private String module;
        private String operationType;
        private String description;
        private String method;
        private String requestMethod;
        private String requestUrl;
        private String requestParams;
        private String responseResult;
        private Long duration;
        private Boolean success;
        private String errorMessage;
        private String ip;
        private String userAgent;
        private LocalDateTime operationTime;
        private LocalDateTime createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder module(String module) {
            this.module = module;
            return this;
        }

        public Builder operationType(String operationType) {
            this.operationType = operationType;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder requestMethod(String requestMethod) {
            this.requestMethod = requestMethod;
            return this;
        }

        public Builder requestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
            return this;
        }

        public Builder requestParams(String requestParams) {
            this.requestParams = requestParams;
            return this;
        }

        public Builder responseResult(String responseResult) {
            this.responseResult = responseResult;
            return this;
        }

        public Builder duration(Long duration) {
            this.duration = duration;
            return this;
        }

        public Builder success(Boolean success) {
            this.success = success;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder operationTime(LocalDateTime operationTime) {
            this.operationTime = operationTime;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public OperationLog build() {
            return new OperationLog(this.id, this.traceId, this.userId, this.username, this.module, this.operationType, this.description, this.method, this.requestMethod, this.requestUrl, this.requestParams, this.responseResult, this.duration, this.success, this.errorMessage, this.ip, this.userAgent, this.operationTime, this.createdAt);
        }
    }
}
