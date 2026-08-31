package io.github.loadup.modules.log.client.dto;

/*-
 * #%L
 * Loadup Modules Log Client
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

import java.time.LocalDateTime;

public class AuditLogDTO {

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

    public AuditLogDTO(
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
            LocalDateTime operationTime) {
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
    }

    public AuditLogDTO() {}

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

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
