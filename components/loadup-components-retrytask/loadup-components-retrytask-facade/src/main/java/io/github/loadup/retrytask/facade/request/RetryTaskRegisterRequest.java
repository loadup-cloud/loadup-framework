package io.github.loadup.retrytask.facade.request;

import io.github.loadup.retrytask.facade.model.Priority;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * A request to register a new retry task.
 */
@Data
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
}
