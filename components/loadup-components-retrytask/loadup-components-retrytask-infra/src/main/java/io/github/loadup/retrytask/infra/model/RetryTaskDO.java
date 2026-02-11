package io.github.loadup.retrytask.infra.model;

/*-
 * #%L
 * Loadup Components Retrytask Infrastructure
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
import lombok.Data;

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

    /**
     * Priority weight: 数值越大优先级越高 (10=HIGH, 1=LOW)
     */
    private Integer priority;

    private String lastFailureReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
