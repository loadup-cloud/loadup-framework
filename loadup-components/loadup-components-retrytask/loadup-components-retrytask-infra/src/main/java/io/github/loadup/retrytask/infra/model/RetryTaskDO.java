package io.github.loadup.retrytask.infra.model;

/*-
 * #%L
 * Loadup Components Retrytask Infrastructure
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
 * Represents a retry task entity in the database.
 */
public class RetryTaskDO {

    private Long id;

    private String bizType;

    private String bizId;

    private Integer retryCount;

    private Integer maxRetryCount;

    private LocalDateTime nextRetryTime;

    private String status;

    /**
     * Priority weight: 数值越大优先级越高 (10=HIGH, 1=LOW)
     */
    private Integer priority;

    private String lastFailureReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public RetryTaskDO(
            Long id,
            String bizType,
            String bizId,
            Integer retryCount,
            Integer maxRetryCount,
            LocalDateTime nextRetryTime,
            String status,
            Integer priority,
            String lastFailureReason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.bizType = bizType;
        this.bizId = bizId;
        this.retryCount = retryCount;
        this.maxRetryCount = maxRetryCount;
        this.nextRetryTime = nextRetryTime;
        this.status = status;
        this.priority = priority;
        this.lastFailureReason = lastFailureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public RetryTaskDO() {}

    public Long getId() {
        return this.id;
    }

    public String getBizType() {
        return this.bizType;
    }

    public String getBizId() {
        return this.bizId;
    }

    public Integer getRetryCount() {
        return this.retryCount;
    }

    public Integer getMaxRetryCount() {
        return this.maxRetryCount;
    }

    public LocalDateTime getNextRetryTime() {
        return this.nextRetryTime;
    }

    public String getStatus() {
        return this.status;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public String getLastFailureReason() {
        return this.lastFailureReason;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public void setNextRetryTime(LocalDateTime nextRetryTime) {
        this.nextRetryTime = nextRetryTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setLastFailureReason(String lastFailureReason) {
        this.lastFailureReason = lastFailureReason;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
