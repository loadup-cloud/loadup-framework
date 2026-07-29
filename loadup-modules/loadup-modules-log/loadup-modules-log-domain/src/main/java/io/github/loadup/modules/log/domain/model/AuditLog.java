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
 * Domain model for audit log.
 *
 * <p>Pure POJO — no persistence framework annotations.
 */
public class AuditLog {

    private String id;
    private String userId;
    private String username;
    /**
     * USER / ROLE / PERMISSION / CONFIG / ...
     */
    private String dataType;

    private String dataId;
    /**
     * CREATE / UPDATE / DELETE / ASSIGN / ...
     */
    private String action;

    private String beforeData;
    private String afterData;
    private String diffData;
    private String reason;
    private String ip;
    private LocalDateTime operationTime;
    private LocalDateTime createdAt;

    public AuditLog(
            String id,
            String userId,
            String username,
            String dataType,
            String dataId,
            String action,
            String beforeData,
            String afterData,
            String diffData,
            String reason,
            String ip,
            LocalDateTime operationTime,
            LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.dataType = dataType;
        this.dataId = dataId;
        this.action = action;
        this.beforeData = beforeData;
        this.afterData = afterData;
        this.diffData = diffData;
        this.reason = reason;
        this.ip = ip;
        this.operationTime = operationTime;
        this.createdAt = createdAt;
    }

    public AuditLog() {}

    public String getId() {
        return this.id;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return this.username;
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

    public String getBeforeData() {
        return this.beforeData;
    }

    public String getAfterData() {
        return this.afterData;
    }

    public String getDiffData() {
        return this.diffData;
    }

    public String getReason() {
        return this.reason;
    }

    public String getIp() {
        return this.ip;
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

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public void setBeforeData(String beforeData) {
        this.beforeData = beforeData;
    }

    public void setAfterData(String afterData) {
        this.afterData = afterData;
    }

    public void setDiffData(String diffData) {
        this.diffData = diffData;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                id,
                userId,
                username,
                dataType,
                dataId,
                action,
                beforeData,
                afterData,
                diffData,
                reason,
                ip,
                operationTime,
                createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog other = (AuditLog) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(userId, other.userId)) return false;
        if (!java.util.Objects.equals(username, other.username)) return false;
        if (!java.util.Objects.equals(dataType, other.dataType)) return false;
        if (!java.util.Objects.equals(dataId, other.dataId)) return false;
        if (!java.util.Objects.equals(action, other.action)) return false;
        if (!java.util.Objects.equals(beforeData, other.beforeData)) return false;
        if (!java.util.Objects.equals(afterData, other.afterData)) return false;
        if (!java.util.Objects.equals(diffData, other.diffData)) return false;
        if (!java.util.Objects.equals(reason, other.reason)) return false;
        if (!java.util.Objects.equals(ip, other.ip)) return false;
        if (!java.util.Objects.equals(operationTime, other.operationTime)) return false;
        if (!java.util.Objects.equals(createdAt, other.createdAt)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "AuditLog(" + "id=" + id + ", " + "userId=" + userId + ", " + "username=" + username + ", " + "dataType="
                + dataType + ", " + "dataId=" + dataId + ", " + "action=" + action + ", " + "beforeData=" + beforeData
                + ", " + "afterData=" + afterData + ", " + "diffData=" + diffData + ", " + "reason=" + reason + ", "
                + "ip=" + ip + ", " + "operationTime=" + operationTime + ", " + "createdAt=" + createdAt + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String userId;
        private String username;
        private String dataType;
        private String dataId;
        private String action;
        private String beforeData;
        private String afterData;
        private String diffData;
        private String reason;
        private String ip;
        private LocalDateTime operationTime;
        private LocalDateTime createdAt;

        public Builder id(String id) {
            this.id = id;
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

        public Builder dataType(String dataType) {
            this.dataType = dataType;
            return this;
        }

        public Builder dataId(String dataId) {
            this.dataId = dataId;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder beforeData(String beforeData) {
            this.beforeData = beforeData;
            return this;
        }

        public Builder afterData(String afterData) {
            this.afterData = afterData;
            return this;
        }

        public Builder diffData(String diffData) {
            this.diffData = diffData;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
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

        public AuditLog build() {
            return new AuditLog(
                    this.id,
                    this.userId,
                    this.username,
                    this.dataType,
                    this.dataId,
                    this.action,
                    this.beforeData,
                    this.afterData,
                    this.diffData,
                    this.reason,
                    this.ip,
                    this.operationTime,
                    this.createdAt);
        }
    }
}
