package io.github.loadup.retrytask.facade.request;

/*-
 * #%L
 * Loadup Components Retrytask Facade
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

import io.github.loadup.retrytask.facade.enums.Priority;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * A request to register a new retry task.
 */
public class RetryTaskRegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The business type of the task.
     */
    private String bizType;

    /**
     * The business identifier of the task.
     */
    private String bizId;

    /**
     * The priority of the task.
     */
    private Priority priority;
    /**
     * next run time
     */
    private LocalDateTime nextRetryTime;

    /**
     * The arguments for the task execution.
     */
    private Map<String, String> args;

    /**
     * Whether to execute the task immediately after registration.
     * If null, follows the global/biz-type configuration.
     */
    private Boolean executeImmediately;

    /**
     * Whether to wait for the execution result if executing immediately.
     * Only works when executeImmediately is true.
     * If true, the registration method will block until the task is processed.
     */
    private Boolean waitResult;

    public RetryTaskRegisterRequest(String bizType, String bizId, Priority priority, LocalDateTime nextRetryTime, Map<String, String> args, Boolean executeImmediately, Boolean waitResult) {
        this.bizType = bizType;
        this.bizId = bizId;
        this.priority = priority;
        this.nextRetryTime = nextRetryTime;
        this.args = args;
        this.executeImmediately = executeImmediately;
        this.waitResult = waitResult;
    }

    public RetryTaskRegisterRequest() {
    }

    public String getBizType() {
        return this.bizType;
    }

    public String getBizId() {
        return this.bizId;
    }

    public Priority getPriority() {
        return this.priority;
    }

    public LocalDateTime getNextRetryTime() {
        return this.nextRetryTime;
    }

    public Map<String, String> getArgs() {
        return this.args;
    }

    public Boolean isExecuteImmediately() {
        return this.executeImmediately;
    }

    public Boolean isWaitResult() {
        return this.waitResult;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setNextRetryTime(LocalDateTime nextRetryTime) {
        this.nextRetryTime = nextRetryTime;
    }

    public void setArgs(Map<String, String> args) {
        this.args = args;
    }

    public void setExecuteImmediately(Boolean executeImmediately) {
        this.executeImmediately = executeImmediately;
    }

    public void setWaitResult(Boolean waitResult) {
        this.waitResult = waitResult;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(bizType, bizId, priority, nextRetryTime, args, executeImmediately, waitResult);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetryTaskRegisterRequest other = (RetryTaskRegisterRequest) o;
        if (!java.util.Objects.equals(bizType, other.bizType)) return false;
        if (!java.util.Objects.equals(bizId, other.bizId)) return false;
        if (!java.util.Objects.equals(priority, other.priority)) return false;
        if (!java.util.Objects.equals(nextRetryTime, other.nextRetryTime)) return false;
        if (!java.util.Objects.equals(args, other.args)) return false;
        if (!java.util.Objects.equals(executeImmediately, other.executeImmediately)) return false;
        if (!java.util.Objects.equals(waitResult, other.waitResult)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "RetryTaskRegisterRequest(" + "bizType=" + bizType + ", " + "bizId=" + bizId + ", " + "priority=" + priority + ", " + "nextRetryTime=" + nextRetryTime + ", " + "args=" + args + ", " + "executeImmediately=" + executeImmediately + ", " + "waitResult=" + waitResult + ")";
    }
}
