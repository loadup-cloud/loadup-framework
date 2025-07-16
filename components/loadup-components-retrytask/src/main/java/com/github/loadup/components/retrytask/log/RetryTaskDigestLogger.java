package com.github.loadup.components.retrytask.log;

import com.github.loadup.components.retrytask.model.RetryTask;

public interface RetryTaskDigestLogger {
    void logStart(RetryTask task);

    void logSuccess(RetryTask task);

    void logRetry(RetryTask task, Exception e);

    void logFailed(RetryTask task, Exception e);
}
