package io.github.loadup.retrytask.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A registry for {@link RetryTaskProcessor}s.
 */
@Component
public class RetryTaskProcessorRegistry {

    private final Map<String, RetryTaskProcessor> processors = new ConcurrentHashMap<>();

    @Autowired
    public RetryTaskProcessorRegistry(List<RetryTaskProcessor> processorList) {
        for (RetryTaskProcessor processor : processorList) {
            processors.put(processor.getBizType(), processor);
        }
    }

    /**
     * Gets the processor for the given business type.
     *
     * @param bizType The business type.
     * @return The processor, or {@code null} if no processor is found.
     */
    public RetryTaskProcessor getProcessor(String bizType) {
        return processors.get(bizType);
    }
}
