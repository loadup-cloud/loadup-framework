package io.github.loadup.retrytask.infra.mysql.model;

import io.github.loadup.retrytask.facade.model.Priority;
import io.github.loadup.retrytask.facade.model.RetryTaskStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Represents a retry task entity in the database.
 */
@Data
@Table("retry_task")
public class RetryTaskDO {

    @Id
    @Column("id")
    private Long id;

    @Column("biz_type")
    private String bizType;

    @Column("biz_id")
    private String bizId;

    @Column("retry_count")
    private Integer retryCount;

    @Column("max_retry_count")
    private Integer maxRetryCount;

    @Column("next_retry_time")
    private LocalDateTime nextRetryTime;

    @Column("status")
    private RetryTaskStatus status;

    @Column("priority")
    private Priority priority;

    @Column("last_failure_reason")
    private String lastFailureReason;

    @Column("create_time")
    private LocalDateTime createTime;

    @Column("update_time")
    private LocalDateTime updateTime;
}
