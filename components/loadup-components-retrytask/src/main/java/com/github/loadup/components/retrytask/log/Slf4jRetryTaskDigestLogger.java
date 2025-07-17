package com.github.loadup.components.retrytask.log;

import com.github.loadup.components.retrytask.constant.RetryTaskLoggerConstants;
import com.github.loadup.components.retrytask.model.RetryTaskDO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j(topic = RetryTaskLoggerConstants.EXECUTE_DIGEST_NAME)
public class Slf4jRetryTaskDigestLogger implements RetryTaskDigestLogger {

    private static final Logger logger = LoggerFactory.getLogger("RetryTaskDigest");

    private final String host = getLocalHost();

    @Override
    public void logStart(RetryTaskDO task) {
        Map<String, Object> context = createContext(task);
        context.put("status", "STARTED");
        context.put("start_time", LocalDateTime.now());

        logger.info("Task execution started: {}", context);
    }

    @Override
    public void logSuccess(RetryTaskDO task) {
        Map<String, Object> context = createContext(task);
        context.put("status", "SUCCESS");
        context.put("end_time", LocalDateTime.now());
        context.put("duration_ms", calculateDuration(task));

        logger.info("Task execution succeeded: {}", context);
    }

    @Override
    public void logRetry(RetryTaskDO task, Exception e) {
        Map<String, Object> context = createContext(task);
        context.put("status", "RETRYING");
        context.put("retry_count", task.getRetryCount() + 1);
        context.put("error_message", e.getMessage());

        logger.warn("Task will retry: {}", context, e);
    }

    @Override
    public void logFailed(RetryTaskDO task, Exception e) {
        Map<String, Object> context = createContext(task);
        context.put("status", "FAILED");
        context.put("retry_count", task.getRetryCount());
        context.put("error_message", e.getMessage());

        logger.error("Task execution failed: {}", context, e);
    }

    private Map<String, Object> createContext(RetryTaskDO task) {
        Map<String, Object> context = new HashMap<>();
        context.put("trace_id", task.getTraceId());
        context.put("task_id", task.getId());
        context.put("task_type", task.getBusinessType());
        context.put("source", task.getSource());
        context.put("max_retries", task.getMaxRetries());
        context.put("host", host);
        context.put("thread", Thread.currentThread().getName());
        return context;
    }

    private long calculateDuration(RetryTaskDO task) {
        return Duration.between(task.getCreatedTime(), LocalDateTime.now()).toMillis();

    }

    private String getLocalHost() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "UNKNOWN";
        }
    }
}
