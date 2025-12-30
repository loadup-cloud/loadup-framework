/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.scheduler.model;

/*-
 * #%L
 * loadup-components-scheduler-api
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.lang.reflect.Method;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

