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
import lombok.Data;

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
}
