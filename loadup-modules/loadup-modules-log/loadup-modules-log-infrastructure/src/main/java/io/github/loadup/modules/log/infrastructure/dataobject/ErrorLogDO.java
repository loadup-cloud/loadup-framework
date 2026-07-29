package io.github.loadup.modules.log.infrastructure.dataobject;

/*-
 * #%L
 * Loadup Modules Log Infrastructure
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

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import java.time.LocalDateTime;

@Table("error_log")
public class ErrorLogDO extends BaseDO {

    private String userId;
    private String errorType;
    private String errorCode;
    private String errorMessage;
    private String stackTrace;
    private String requestUrl;
    private String requestMethod;
    private String requestParams;
    private String ip;
    private LocalDateTime errorTime;

    public ErrorLogDO(String userId, String errorType, String errorCode, String errorMessage, String stackTrace, String requestUrl, String requestMethod, String requestParams, String ip, LocalDateTime errorTime) {
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

    public ErrorLogDO() {
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

    public LocalDateTime getErrorTime() {
        return this.errorTime;
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

    public void setErrorTime(LocalDateTime errorTime) {
        this.errorTime = errorTime;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), userId, errorType, errorCode, errorMessage, stackTrace, requestUrl, requestMethod, requestParams, ip, errorTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ErrorLogDO other = (ErrorLogDO) o;
        if (!java.util.Objects.equals(userId, other.userId)) return false;
        if (!java.util.Objects.equals(errorType, other.errorType)) return false;
        if (!java.util.Objects.equals(errorCode, other.errorCode)) return false;
        if (!java.util.Objects.equals(errorMessage, other.errorMessage)) return false;
        if (!java.util.Objects.equals(stackTrace, other.stackTrace)) return false;
        if (!java.util.Objects.equals(requestUrl, other.requestUrl)) return false;
        if (!java.util.Objects.equals(requestMethod, other.requestMethod)) return false;
        if (!java.util.Objects.equals(requestParams, other.requestParams)) return false;
        if (!java.util.Objects.equals(ip, other.ip)) return false;
        if (!java.util.Objects.equals(errorTime, other.errorTime)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "ErrorLogDO(" + "super=" + super.toString() + ", " + "userId=" + userId + ", " + "errorType=" + errorType + ", " + "errorCode=" + errorCode + ", " + "errorMessage=" + errorMessage + ", " + "stackTrace=" + stackTrace + ", " + "requestUrl=" + requestUrl + ", " + "requestMethod=" + requestMethod + ", " + "requestParams=" + requestParams + ", " + "ip=" + ip + ", " + "errorTime=" + errorTime + ")";
    }
}
