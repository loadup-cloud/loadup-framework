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

public class OperationLogQuery {

    private String userId;
    private String module;
    private String operationType;
    private Boolean success;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer pageNum;
    private Integer pageSize;

    public OperationLogQuery(
            String userId,
            String module,
            String operationType,
            Boolean success,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer pageNum,
            Integer pageSize) {
        this.userId = userId;
        this.module = module;
        this.operationType = operationType;
        this.success = success;
        this.startTime = startTime;
        this.endTime = endTime;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public OperationLogQuery() {}

    public String getUserId() {
        return this.userId;
    }

    public String getModule() {
        return this.module;
    }

    public String getOperationType() {
        return this.operationType;
    }

    public Boolean isSuccess() {
        return this.success;
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

    public void setModule(String module) {
        this.module = module;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
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
    public int hashCode() {
        return java.util.Objects.hash(userId, module, operationType, success, startTime, endTime, pageNum, pageSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationLogQuery other = (OperationLogQuery) o;
        if (!java.util.Objects.equals(userId, other.userId)) return false;
        if (!java.util.Objects.equals(module, other.module)) return false;
        if (!java.util.Objects.equals(operationType, other.operationType)) return false;
        if (!java.util.Objects.equals(success, other.success)) return false;
        if (!java.util.Objects.equals(startTime, other.startTime)) return false;
        if (!java.util.Objects.equals(endTime, other.endTime)) return false;
        if (!java.util.Objects.equals(pageNum, other.pageNum)) return false;
        if (!java.util.Objects.equals(pageSize, other.pageSize)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "OperationLogQuery(" + "userId=" + userId + ", " + "module=" + module + ", " + "operationType="
                + operationType + ", " + "success=" + success + ", " + "startTime=" + startTime + ", " + "endTime="
                + endTime + ", " + "pageNum=" + pageNum + ", " + "pageSize=" + pageSize + ")";
    }
}
