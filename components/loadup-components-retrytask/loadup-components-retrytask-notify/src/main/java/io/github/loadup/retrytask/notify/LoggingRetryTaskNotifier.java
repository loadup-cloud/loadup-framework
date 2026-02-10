package io.github.loadup.retrytask.notify;

import io.github.loadup.retrytask.facade.model.RetryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A simple implementation of {@link RetryTaskNotifier} that logs the notification.
 */
@Component
public class LoggingRetryTaskNotifier implements RetryTaskNotifier {

    private static final Logger logger = LoggerFactory.getLogger(LoggingRetryTaskNotifier.class);

    @Override
    public void notify(RetryTask task) {
        logger.warn("Task failed: bizType={}, bizId={}", task.getBizType(), task.getBizId());
    }

    @Override
    public String getType() {
        return "";
    }
}
