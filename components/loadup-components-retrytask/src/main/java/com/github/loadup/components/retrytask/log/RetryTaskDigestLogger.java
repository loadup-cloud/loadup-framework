package com.github.loadup.components.retrytask.log;

import com.github.loadup.components.retrytask.model.RetryTaskDO;

public interface RetryTaskDigestLogger {
    void logStart(RetryTaskDO task);

    void logSuccess(RetryTaskDO task);

    void logRetry(RetryTaskDO task, Exception e);

    void logFailed(RetryTaskDO task, Exception e);
}
