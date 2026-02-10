package io.github.loadup.retrytask.facade.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

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
