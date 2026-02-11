package io.github.loadup.retrytask.facade.model;

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
import io.github.loadup.retrytask.facade.enums.RetryTaskStatus;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Represents a retry task.
 */
@Data
public class RetryTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier of the task.
     */
    private Long id;

    /**
     * The business type of the task.
     */
    private String bizType;

    /**
     * The business identifier of the task.
     */
    private String bizId;

    /**
     * The number of times the task has been retried.
     */
    private Integer retryCount;

    /**
     * The maximum number of times the task can be retried.
     */
    private Integer maxRetryCount;

    /**
     * The time when the task will be retried next.
     */
    private LocalDateTime nextRetryTime;

    /**
     * The status of the task.
     */
    private RetryTaskStatus status;

    /**
     * The priority of the task.
     */
    private Priority priority;

    /**
     * The reason for the last failure.
     */
    private String lastFailureReason;

    /**
     * The time when the task was created.
     */
    private LocalDateTime createTime;

    /**
     * The time when the task was last updated.
     */
    private LocalDateTime updateTime;
}
