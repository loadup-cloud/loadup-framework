package io.github.loadup.retrytask.infra.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents a retry task entity in the database.
 */
@Data
public class RetryTaskDO {

    private Long id;

    private String bizType;

    private String bizId;

    private Integer retryCount;

    private Integer maxRetryCount;

    private LocalDateTime nextRetryTime;

    private String status;

    private String priority;

    private String lastFailureReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
