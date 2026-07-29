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

public class LogStatisticsQuery {

    private String userId;
    private String module;
    private String operationType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public LogStatisticsQuery(
            String userId, String module, String operationType, LocalDateTime startTime, LocalDateTime endTime) {
        this.userId = userId;
        this.module = module;
        this.operationType = operationType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LogStatisticsQuery() {}

    public String getUserId() {
        return this.userId;
    }

    public String getModule() {
        return this.module;
    }

    public String getOperationType() {
        return this.operationType;
    }

    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    public LocalDateTime getEndTime() {
        return this.endTime;
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

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
