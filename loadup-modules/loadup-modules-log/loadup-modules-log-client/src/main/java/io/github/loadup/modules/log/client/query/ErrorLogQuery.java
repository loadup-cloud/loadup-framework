package io.github.loadup.modules.log.client.query;

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

import java.time.LocalDateTime;

public class ErrorLogQuery {

    private String userId;
    private String errorType;
    private String errorCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer pageNum;
    private Integer pageSize;

    public ErrorLogQuery(
            String userId,
            String errorType,
            String errorCode,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer pageNum,
            Integer pageSize) {
        this.userId = userId;
        this.errorType = errorType;
        this.errorCode = errorCode;
        this.startTime = startTime;
        this.endTime = endTime;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public ErrorLogQuery() {}

    public String getUserId() {
        return this.userId;
    }

    public String getErrorType() {
        return this.errorType;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    public LocalDateTime getEndTime() {
        return this.endTime;
    }

    public Integer getPageNum() {
        return this.pageNum;
    }

    public Integer getPageSize() {
        return this.pageSize;
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

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
