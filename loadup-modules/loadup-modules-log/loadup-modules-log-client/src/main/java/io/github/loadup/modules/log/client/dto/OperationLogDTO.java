package io.github.loadup.modules.log.client.dto;

/*-
 * #%L
 * Loadup Modules Log Client
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

public class OperationLogDTO {

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

    public OperationLogDTO(
            String id,
            String traceId,
            String userId,
            String username,
            String module,
            String operationType,
            String description,
            String method,
            String requestMethod,
            String requestUrl,
            String requestParams,
            String responseResult,
            Long duration,
            Boolean success,
            String errorMessage,
            String ip,
            String userAgent,
            LocalDateTime operationTime) {
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
    }

    public OperationLogDTO() {}

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

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
