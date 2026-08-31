package io.github.loadup.components.globalunique.entity;

/*-
 * #%L
 * LoadUp Components :: Global Unique
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

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
