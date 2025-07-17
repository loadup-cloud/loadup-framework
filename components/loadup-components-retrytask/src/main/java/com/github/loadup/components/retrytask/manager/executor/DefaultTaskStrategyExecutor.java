package com.github.loadup.components.retrytask.manager.executor;

import com.github.loadup.components.retrytask.manager.TaskStrategyExecutor;
import com.github.loadup.components.retrytask.model.RetryTaskDO;
import org.springframework.stereotype.Component;

@Component
public class DefaultTaskStrategyExecutor implements TaskStrategyExecutor {
    public void execute(RetryTaskDO retryTask) {
    }
}
