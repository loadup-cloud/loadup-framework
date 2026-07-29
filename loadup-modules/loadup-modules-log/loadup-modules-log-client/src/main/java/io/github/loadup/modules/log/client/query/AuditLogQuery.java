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

public class AuditLogQuery {

    private String userId;
    private String dataType;
    private String dataId;
    private String action;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer pageNum;
    private Integer pageSize;

    public AuditLogQuery(
            String userId,
            String dataType,
            String dataId,
            String action,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer pageNum,
            Integer pageSize) {
        this.userId = userId;
        this.dataType = dataType;
        this.dataId = dataId;
        this.action = action;
        this.startTime = startTime;
        this.endTime = endTime;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public AuditLogQuery() {}

    public String getUserId() {
        return this.userId;
    }

    public String getDataType() {
        return this.dataType;
    }

    public String getDataId() {
        return this.dataId;
    }

    public String getAction() {
        return this.action;
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

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public void setAction(String action) {
        this.action = action;
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
        return java.util.Objects.hash(userId, dataType, dataId, action, startTime, endTime, pageNum, pageSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLogQuery other = (AuditLogQuery) o;
        if (!java.util.Objects.equals(userId, other.userId)) return false;
        if (!java.util.Objects.equals(dataType, other.dataType)) return false;
        if (!java.util.Objects.equals(dataId, other.dataId)) return false;
        if (!java.util.Objects.equals(action, other.action)) return false;
        if (!java.util.Objects.equals(startTime, other.startTime)) return false;
        if (!java.util.Objects.equals(endTime, other.endTime)) return false;
        if (!java.util.Objects.equals(pageNum, other.pageNum)) return false;
        if (!java.util.Objects.equals(pageSize, other.pageSize)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "AuditLogQuery(" + "userId=" + userId + ", " + "dataType=" + dataType + ", " + "dataId=" + dataId + ", "
                + "action=" + action + ", " + "startTime=" + startTime + ", " + "endTime=" + endTime + ", " + "pageNum="
                + pageNum + ", " + "pageSize=" + pageSize + ")";
    }
}
