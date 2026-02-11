package io.github.loadup.retrytask.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for {@link RetryTaskProcessor}s
 */
public class RetryTaskProcessorRegistry {

    private final Map<String, RetryTaskProcessor> processors = new ConcurrentHashMap<>();

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
