package com.github.loadup.components.retrytask.manager;

import com.github.loadup.components.retrytask.constant.ScheduleExecuteType;
import com.github.loadup.components.retrytask.model.RetryTaskDO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RetryTaskExecuteManager {
    @Resource
    private TaskStrategyExecutorFactory taskStrategyExecutorFactory;

    public void execute(RetryTaskDO retryTask, ScheduleExecuteType scheduleExecuteType) {
        if (scheduleExecuteType != null && retryTask != null) {
            TaskStrategyExecutor taskStrategyExecutor = this.taskStrategyExecutorFactory.obtainTaskStrategyExecutor(scheduleExecuteType);
            taskStrategyExecutor.execute(retryTask);
        } else {
            log.warn("RetryTaskExecuteManager parameter illegal, scheduleExecuteType={},retryTask={}", scheduleExecuteType, retryTask);
        }
    }

    public TaskStrategyExecutorFactory getTaskStrategyExecutorFactory() {
        return this.taskStrategyExecutorFactory;
    }

    public void setTaskStrategyExecutorFactory(TaskStrategyExecutorFactory taskStrategyExecutorFactory) {
        this.taskStrategyExecutorFactory = taskStrategyExecutorFactory;
    }
}
