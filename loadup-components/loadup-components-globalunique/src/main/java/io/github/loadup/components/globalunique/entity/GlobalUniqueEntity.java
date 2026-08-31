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
 * Idempotency ledger record stored in the {@code global_unique} table.
 */
public class GlobalUniqueEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key. */
    private String id;

    /** Tenant id (optional). */
    private String tenantId;

    /** Business-defined unique key, e.g. {@code ORDER_CREATE:userId:orderNo}. */
    private String uniqueKey;

    /** Business type used for classification, e.g. {@code ORDER}, {@code PAYMENT}. */
    private String bizType;

    /** Business id (optional). */
    private String bizId;

    /** Request data snapshot as JSON (optional, for troubleshooting). */
    private String requestData;

    /** Creation time. */
    private LocalDateTime createdAt;

    /** Last update time. */
    private LocalDateTime updatedAt;

    /** Logical delete flag. */
    private Boolean deleted = false;

    public GlobalUniqueEntity(
            String id,
            String tenantId,
            String uniqueKey,
            String bizType,
            String bizId,
            String requestData,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean deleted) {
        this.id = id;
        this.tenantId = tenantId;
        this.uniqueKey = uniqueKey;
        this.bizType = bizType;
        this.bizId = bizId;
        this.requestData = requestData;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    public GlobalUniqueEntity() {}

    public String getId() {
        return this.id;
    }

    public String getTenantId() {
        return this.tenantId;
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

    public Boolean isDeleted() {
        return this.deleted;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String tenantId;
        private String uniqueKey;
        private String bizType;
        private String bizId;
        private String requestData;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Boolean deleted = false;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
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

        public Builder deleted(Boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public GlobalUniqueEntity build() {
            return new GlobalUniqueEntity(
                    this.id,
                    this.tenantId,
                    this.uniqueKey,
                    this.bizType,
                    this.bizId,
                    this.requestData,
                    this.createdAt,
                    this.updatedAt,
                    this.deleted);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
