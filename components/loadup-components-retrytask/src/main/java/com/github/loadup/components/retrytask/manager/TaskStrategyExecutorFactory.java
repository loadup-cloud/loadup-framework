package com.github.loadup.components.retrytask.manager;

import com.github.loadup.components.retrytask.constant.ScheduleExecuteType;
import com.github.loadup.components.retrytask.manager.executor.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TaskStrategyExecutorFactory {
    @Resource
    private DefaultTaskStrategyExecutor       defaultTaskStrategyExecutor;
    @Resource
    private SyncTaskStrategyExecutor          syncTaskStrategyExecutor;
    @Resource
    private AsyncTaskStrategyExecutor         asyncTaskStrategyExecutor;
    private Map<String, TaskStrategyExecutor> taskStrategyExecutors = new HashMap<>();

    public TaskStrategyExecutor obtainTaskStrategyExecutor(ScheduleExecuteType scheduleExecuteType) {
        return this.taskStrategyExecutors.get(scheduleExecuteType.getCode());
    }

    public Map<String, TaskStrategyExecutor> getTaskStrategyExecutors() {
        return this.taskStrategyExecutors;
    }

    @PostConstruct
    public void init() {
        taskStrategyExecutors.put(ScheduleExecuteType.DEFAULT.getCode(), defaultTaskStrategyExecutor);
        taskStrategyExecutors.put(ScheduleExecuteType.ASYNC.getCode(), asyncTaskStrategyExecutor);
        taskStrategyExecutors.put(ScheduleExecuteType.SYNC.getCode(), syncTaskStrategyExecutor);
    }

}
