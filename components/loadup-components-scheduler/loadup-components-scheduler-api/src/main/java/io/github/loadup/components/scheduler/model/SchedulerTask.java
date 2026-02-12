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
import java.util.concurrent.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

/**
 * Scheduler task model representing a scheduled task configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class SchedulerTask {

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
    @Builder.Default
    private boolean enabled = true;

    /**
     * Bean instance containing the method
     */
    private Object targetBean;

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
     * 利用 CompletableFuture 实现超时控制
     */
    private void executeWithTimeout(ExecutorService executor) throws Exception {
        CompletableFuture<Void> future = CompletableFuture.runAsync(this::invokeRaw, executor);
        try {
            future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true); // 尝试中断正在执行的线程
            throw new RuntimeException("Task execution timed out after " + timeoutMillis + "ms");
        } catch (ExecutionException e) {
            // 提取真正的业务异常
            Throwable cause = e.getCause();
            if (cause instanceof Exception) throw (Exception) cause;
            throw new RuntimeException(cause);
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
            Thread.sleep(Math.min(sleepTime, 30000L)); // 最高等待 30s
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
