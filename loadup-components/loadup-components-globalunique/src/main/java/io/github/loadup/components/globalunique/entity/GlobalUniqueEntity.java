package io.github.loadup.components.globalunique.entity;

/*-
 * #%L
 * LoadUp Components :: Global Unique
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

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 全局唯一性记录实体
 *
 * @author loadup
 */
public class GlobalUniqueEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 唯一键（业务方自定义）
     */
    private String uniqueKey;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 业务ID（可选）
     */
    private String bizId;

    /**
     * 请求数据快照（可选，JSON格式）
     */
    private String requestData;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    public GlobalUniqueEntity(
            String id,
            String uniqueKey,
            String bizType,
            String bizId,
            String requestData,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.uniqueKey = uniqueKey;
        this.bizType = bizType;
        this.bizId = bizId;
        this.requestData = requestData;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public GlobalUniqueEntity() {}

    public String getId() {
        return this.id;
    }

    public String getUniqueKey() {
        return this.uniqueKey;
    }

    public String getBizType() {
        return this.bizType;
    }

    public String getBizId() {
        return this.bizId;
    }

    public String getRequestData() {
        return this.requestData;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, uniqueKey, bizType, bizId, requestData, createdAt, updatedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalUniqueEntity other = (GlobalUniqueEntity) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(uniqueKey, other.uniqueKey)) return false;
        if (!java.util.Objects.equals(bizType, other.bizType)) return false;
        if (!java.util.Objects.equals(bizId, other.bizId)) return false;
        if (!java.util.Objects.equals(requestData, other.requestData)) return false;
        if (!java.util.Objects.equals(createdAt, other.createdAt)) return false;
        if (!java.util.Objects.equals(updatedAt, other.updatedAt)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "GlobalUniqueEntity(" + "id=" + id + ", " + "uniqueKey=" + uniqueKey + ", " + "bizType=" + bizType + ", "
                + "bizId=" + bizId + ", " + "requestData=" + requestData + ", " + "createdAt=" + createdAt + ", "
                + "updatedAt=" + updatedAt + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String uniqueKey;
        private String bizType;
        private String bizId;
        private String requestData;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder uniqueKey(String uniqueKey) {
            this.uniqueKey = uniqueKey;
            return this;
        }

        public Builder bizType(String bizType) {
            this.bizType = bizType;
            return this;
        }

        public Builder bizId(String bizId) {
            this.bizId = bizId;
            return this;
        }

        public Builder requestData(String requestData) {
            this.requestData = requestData;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public GlobalUniqueEntity build() {
            return new GlobalUniqueEntity(
                    this.id,
                    this.uniqueKey,
                    this.bizType,
                    this.bizId,
                    this.requestData,
                    this.createdAt,
                    this.updatedAt);
        }
    }
}
