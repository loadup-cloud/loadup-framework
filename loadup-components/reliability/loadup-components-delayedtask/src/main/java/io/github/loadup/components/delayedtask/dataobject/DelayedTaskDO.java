/*
 * #%L
 * LoadUp Components DelayedTask
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
package io.github.loadup.components.delayedtask.dataobject;

import io.github.loadup.commons.dataobject.BaseDO;
import io.github.loadup.components.delayedtask.DelayedTaskStatus;
import java.time.LocalDateTime;

public class DelayedTaskDO extends BaseDO {
    private String taskType;
    private String bizId;
    private LocalDateTime executeAt;
    private LocalDateTime nextExecuteAt;
    private DelayedTaskStatus status;
    private Integer attempts;
    private Integer maxAttempts;
    private LocalDateTime leaseUntil;
    private String claimToken;
    private String lastError;

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String value) {
        this.taskType = value;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String value) {
        this.bizId = value;
    }

    public LocalDateTime getExecuteAt() {
        return executeAt;
    }

    public void setExecuteAt(LocalDateTime value) {
        this.executeAt = value;
    }

    public LocalDateTime getNextExecuteAt() {
        return nextExecuteAt;
    }

    public void setNextExecuteAt(LocalDateTime value) {
        this.nextExecuteAt = value;
    }

    public DelayedTaskStatus getStatus() {
        return status;
    }

    public void setStatus(DelayedTaskStatus value) {
        this.status = value;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int value) {
        this.attempts = value;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int value) {
        this.maxAttempts = value;
    }

    public LocalDateTime getLeaseUntil() {
        return leaseUntil;
    }

    public void setLeaseUntil(LocalDateTime value) {
        this.leaseUntil = value;
    }

    public String getClaimToken() {
        return claimToken;
    }

    public void setClaimToken(String value) {
        this.claimToken = value;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String value) {
        this.lastError = value;
    }
}
