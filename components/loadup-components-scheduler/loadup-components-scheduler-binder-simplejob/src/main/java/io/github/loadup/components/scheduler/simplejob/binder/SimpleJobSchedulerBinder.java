package io.github.loadup.components.scheduler.simplejob.binder;

/*-
 * #%L
 * loadup-components-scheduler-binder-simplejob
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

import io.github.loadup.components.scheduler.binder.AbstractSchedulerBinder;
import io.github.loadup.components.scheduler.binder.SchedulerBinder;
import io.github.loadup.components.scheduler.cfg.SchedulerBindingCfg;
import io.github.loadup.components.scheduler.model.SchedulerTask;
import io.github.loadup.components.scheduler.simplejob.cfg.SimpleJobSchedulerBinderCfg;
import io.github.loadup.components.scheduler.simplejob.context.CustomThreadFactory;
import io.github.loadup.components.scheduler.simplejob.context.TaskExecutionContext;
import java.util.Map;
import java.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

/**
 * Simple job scheduler binder implementation using Spring's TaskScheduler. This is a lightweight
 * implementation suitable for single-instance applications.
 */
@Slf4j
public class SimpleJobSchedulerBinder extends AbstractSchedulerBinder<SimpleJobSchedulerBinderCfg, SchedulerBindingCfg>
        implements SchedulerBinder<SimpleJobSchedulerBinderCfg, SchedulerBindingCfg> {

    // 负责计时的调度器
    private ThreadPoolTaskScheduler taskScheduler;
    // 负责执行业务逻辑的线程池（支持超时和重试）
    private ExecutorService businessExecutor;

    // 核心缓存：任务名 -> 运行上下文
    private final Map<String, TaskExecutionContext> taskRegistry = new ConcurrentHashMap<>();

    @Override
    protected void onInit() {
        // 1. 初始化计时调度器
        this.taskScheduler = new ThreadPoolTaskScheduler();
        this.taskScheduler.setPoolSize(getBinderCfg().getPoolSize());
        this.taskScheduler.setThreadNamePrefix("scheduler-" + getName() + "-");
        this.taskScheduler.initialize();

        // 2. 初始化业务执行池 (用于处理 SchedulerTask 中的超时控制)
        this.businessExecutor = new ThreadPoolExecutor(
                getBinderCfg().getPoolSize(),
                getBinderCfg().getPoolSize() * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new CustomThreadFactory("exec-" + getName(), false), // 关键点
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public String getBinderType() {
        return "simplejob";
    }

    @Override
    public void binderDestroy() {
        log.info("Destroying SimpleJob scheduler binder");
        taskRegistry.values().forEach(ctx -> ctx.getFuture().cancel(true));
        taskScheduler.shutdown();
        businessExecutor.shutdown();
    }

    @Override
    public boolean schedule(SchedulerTask task) {
        String taskName = task.getTaskName();
        cancel(taskName); // 确保幂等，如果存在则先停止

        if (!task.isEnabled()) return false;

        // 包装为带超时和重试的安全 Runnable
        Runnable runnable = task.toRunnable(businessExecutor);

        // 提交 Spring 调度
        ScheduledFuture<?> future = taskScheduler.schedule(runnable, new CronTrigger(task.getCron()));

        // 存入注册表
        taskRegistry.put(taskName, new TaskExecutionContext(task, future, false));
        log.info("Task [{}] scheduled with cron: [{}]", taskName, task.getCron());
        return true;
    }

    @Override
    public boolean pauseTask(String taskName) {
        TaskExecutionContext ctx = taskRegistry.get(taskName);
        if (ctx == null || ctx.isPaused()) {
            return false;
        }
        // Spring 无法真正暂停线程，只能取消当前的 Future
        ctx.getFuture().cancel(true);
        ctx.setPaused(true);
        log.info("Task [{}] paused.", taskName);
        return true;
    }

    @Override
    public boolean resumeTask(String taskName) {
        TaskExecutionContext ctx = taskRegistry.get(taskName);
        if (ctx == null || !ctx.isPaused()) {
            return false;
        }
        // 重新调用调度逻辑
        Runnable runnable = ctx.getTask().toRunnable(businessExecutor);
        ScheduledFuture<?> future =
                taskScheduler.schedule(runnable, new CronTrigger(ctx.getTask().getCron()));

        ctx.setFuture(future);
        ctx.setPaused(false);
        log.info("Task [{}] resumed.", taskName);
        return true;
    }

    @Override
    public boolean triggerTask(String taskName) {
        TaskExecutionContext ctx = taskRegistry.get(taskName);
        if (ctx == null) {
            return false;
        }
        // 立即异步执行一次，不影响原本的 Cron 周期
        businessExecutor.execute(ctx.getTask().toRunnable(businessExecutor));
        log.info("Task [{}] triggered manually.", taskName);
        return true;
    }

    @Override
    public boolean updateTaskCron(String taskName, String newCron) {
        TaskExecutionContext ctx = taskRegistry.get(taskName);
        if (ctx == null) {
            return false;
        }
        ctx.getTask().setCron(newCron);
        // 重新 schedule 会自动处理取消和重启
        schedule(ctx.getTask());
        log.info("Task [{}] cron updated to: [{}]", taskName, newCron);
        return true;
    }

    @Override
    public boolean taskExists(String taskName) {
        return taskRegistry.containsKey(taskName);
    }

    @Override
    public boolean cancel(String taskName) {
        TaskExecutionContext ctx = taskRegistry.remove(taskName);
        if (ctx != null && ctx.getFuture() != null) {
            ctx.getFuture().cancel(true);
        }
        return true;
    }
}
