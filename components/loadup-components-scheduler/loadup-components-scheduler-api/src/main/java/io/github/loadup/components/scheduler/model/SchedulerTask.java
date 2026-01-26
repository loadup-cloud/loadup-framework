package io.github.loadup.components.scheduler.model;

/*-
 * #%L
 * loadup-components-scheduler-api
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
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

import java.lang.reflect.Method;
import java.util.Map;
import lombok.*;

/**
 * Scheduler task model representing a scheduled task configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerTask {

    /**
     * Unique name of the task
     */
    private String taskName;

    /**
     * Cron expression for scheduling
     */
    private String cron;

    /**
     * Task description
     */
    private String description;

    /**
     * Task group for categorization
     */
    private String taskGroup;

    /**
     * Method to be invoked
     */
    private Method method;

    /**
     * Bean instance containing the method
     */
    private Object targetBean;

    /**
     * Annotation class that triggered this task registration
     */
    private Class<?> annotation;

    /**
     * Additional parameters for the task
     */
    private Map<String, Object> parameters;

    /**
     * Whether task is enabled
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Priority of the task (higher value = higher priority)
     */
    @Builder.Default
    private int priority = 0;

    /**
     * Timeout in milliseconds (0 = no timeout)
     */
    @Builder.Default
    private long timeoutMillis = 0;

    /**
     * Maximum retry times on failure
     */
    @Builder.Default
    private int maxRetries = 0;
}

