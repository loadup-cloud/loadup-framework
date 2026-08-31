package io.github.loadup.components.scheduler.model;

/*-
 * #%L
 * loadup-components-scheduler-api
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

/**
 * Scheduler task model representing a scheduled task configuration.
 */
public class SchedulerTask {
    private static final Logger log = LoggerFactory.getLogger(SchedulerTask.class);

    /**
     * Unique name of the task
     */
    private String taskName;
    /**
     * Task group for categorization
     */
    private String taskGroup;

    /**
     * Task description
     */
    private String description;

    /**
     * spring bean name of the target method's class
     */
    private String beanName;
    /**
     * Method to be invoked
     */
    private Method method;
    /**
     * 转换 parameters 后的实际参数列表
     */
    private Object[] args;

    /**
     * Cron expression for scheduling
     */
    private String cron;

    /**
     * Whether task is enabled
     */
    private boolean enabled = true;

    /**
     * Bean instance containing the method
     */
    private Object targetBean;

    /**
     * Timeout in milliseconds (0 = no timeout)
     */
    private long timeoutMillis = 0;

    /**
     * Maximum retry times on failure
     */
    private int maxRetries = 0;

    /**
     * 将反射调用封装为 Runnable，供 Binder 使用
     */
    public Runnable toRunnable(ExecutorService executor) {
        return () -> {
            int attempt = 0;
            boolean success = false;

            while (attempt <= maxRetries && !success) {
                try {
                    if (timeoutMillis > 0) {
                        // 方案 A：带超时的执行
                        executeWithTimeout(executor);
                    } else {
                        // 方案 B：普通执行
                        invokeRaw();
                    }
                    success = true;
                    if (attempt > 0) {
                        log.info("Task [{}] succeeded after {} retries", taskName, attempt);
                    }
                } catch (Exception e) {
                    attempt++;
                    if (attempt > maxRetries) {
                        log.error("Task [{}] failed after {} attempts. Permanent error: ", taskName, attempt, e);
                    } else {
                        log.warn(
                                "Task [{}] failed (attempt {}/{}), retrying... Error: {}",
                                taskName,
                                attempt,
                                maxRetries,
                                e.getMessage());
                        backoff(attempt); // 退避策略
                    }
                }
            }
        };
    }

    /**
     * Uses CompletableFuture to implement timeout-controlled execution.
     */
    private void executeWithTimeout(ExecutorService executor) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(this::invokeRaw, executor);
        try {
            future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true); // Attempt to interrupt the running thread
            throw new RuntimeException("Task execution timed out after " + timeoutMillis + "ms", e);
        } catch (ExecutionException e) {
            // Unwrap the real business exception; always chain e to preserve the full stack trace
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Task execution was interrupted", e);
        }
    }

    /**
     * 核心反射调用逻辑
     */
    private void invokeRaw() {
        try {
            ReflectionUtils.makeAccessible(method);
            // 如果有 parameters，需在此处理参数绑定
            method.invoke(targetBean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 简单的指数退避策略
     */
    private void backoff(int attempt) {
        try {
            // 每次重试等待时间加倍，例如 1s, 2s, 4s...
            long sleepTime = (long) Math.pow(2, attempt - 1) * 1000L;
            Thread.sleep(Math.min(sleepTime, 30_000L)); // Maximum wait: 30 s
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    public SchedulerTask(
            String taskName,
            String taskGroup,
            String description,
            String beanName,
            Method method,
            Object[] args,
            String cron,
            boolean enabled,
            Object targetBean,
            long timeoutMillis,
            int maxRetries) {
        this.taskName = taskName;
        this.taskGroup = taskGroup;
        this.description = description;
        this.beanName = beanName;
        this.method = method;
        this.args = args;
        this.cron = cron;
        this.enabled = enabled;
        this.targetBean = targetBean;
        this.timeoutMillis = timeoutMillis;
        this.maxRetries = maxRetries;
    }

    public SchedulerTask() {}

    public String getTaskName() {
        return this.taskName;
    }

    public String getTaskGroup() {
        return this.taskGroup;
    }

    public String getDescription() {
        return this.description;
    }

    public String getBeanName() {
        return this.beanName;
    }

    public Method getMethod() {
        return this.method;
    }

    public Object[] getArgs() {
        return this.args;
    }

    public String getCron() {
        return this.cron;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Object getTargetBean() {
        return this.targetBean;
    }

    public long getTimeoutMillis() {
        return this.timeoutMillis;
    }

    public int getMaxRetries() {
        return this.maxRetries;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setTaskGroup(String taskGroup) {
        this.taskGroup = taskGroup;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setTargetBean(Object targetBean) {
        this.targetBean = targetBean;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String taskName;
        private String taskGroup;
        private String description;
        private String beanName;
        private Method method;
        private Object[] args;
        private String cron;
        private boolean enabled = true;
        private Object targetBean;
        private long timeoutMillis = 0;
        private int maxRetries = 0;

        public Builder taskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public Builder taskGroup(String taskGroup) {
            this.taskGroup = taskGroup;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder beanName(String beanName) {
            this.beanName = beanName;
            return this;
        }

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public Builder args(Object[] args) {
            this.args = args;
            return this;
        }

        public Builder cron(String cron) {
            this.cron = cron;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder targetBean(Object targetBean) {
            this.targetBean = targetBean;
            return this;
        }

        public Builder timeoutMillis(long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public SchedulerTask build() {
            return new SchedulerTask(
                    this.taskName,
                    this.taskGroup,
                    this.description,
                    this.beanName,
                    this.method,
                    this.args,
                    this.cron,
                    this.enabled,
                    this.targetBean,
                    this.timeoutMillis,
                    this.maxRetries);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
