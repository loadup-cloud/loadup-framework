package io.github.loadup.retrytask.notify;

import io.github.loadup.retrytask.facade.model.RetryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logging implementation of {@link RetryTaskNotifier} that logs task failures
 */
public class LoggingRetryTaskNotifier implements RetryTaskNotifier {

    private static final Logger logger = LoggerFactory.getLogger(LoggingRetryTaskNotifier.class);

    public static final String TYPE = "log";

    @Override
    public void notify(RetryTask task) {
        logger.warn(">>> [RETRY-TASK] Task failed: bizType={}, bizId={}, retryCount={}, failureReason={}",
            task.getBizType(),
            task.getBizId(),
            task.getRetryCount(),
            task.getLastFailureReason());
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
