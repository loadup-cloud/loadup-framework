package com.github.loadup.components.retrytask.registry;

import com.github.loadup.components.retrytask.handler.RetryTaskExecutorHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TaskHandlerRegistry {

    private final Map<String, RetryTaskExecutorHandler> registry = new HashMap<>();

    public void register(String taskType, RetryTaskExecutorHandler handler) {
        registry.put(taskType, handler);
    }

    public RetryTaskExecutorHandler get(String taskType) {
        return registry.get(taskType);
    }
}