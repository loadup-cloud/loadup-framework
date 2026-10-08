package io.github.loadup.retrytask.jobrunr;

import io.github.loadup.retrytask.facade.RetryTaskProcessor;
import io.github.loadup.retrytask.facade.RetryTaskProcessorRegistry;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collects all {@link RetryTaskProcessor} beans and resolves them by business type.
 */
public class DefaultRetryTaskProcessorRegistry implements RetryTaskProcessorRegistry {

    private final Map<String, RetryTaskProcessor> processors;

    public DefaultRetryTaskProcessorRegistry(List<RetryTaskProcessor> processors) {
        this.processors = processors.stream()
                .collect(Collectors.toUnmodifiableMap(
                        RetryTaskProcessor::bizType, Function.identity(), (first, duplicate) -> {
                            throw new IllegalStateException(
                                    "Duplicate RetryTaskProcessor for bizType '" + duplicate.bizType() + "'");
                        }));
    }

    @Override
    public RetryTaskProcessor getProcessor(String bizType) {
        RetryTaskProcessor processor = processors.get(bizType);
        if (processor == null) {
            throw new IllegalArgumentException("No RetryTaskProcessor registered for bizType '" + bizType + "'");
        }
        return processor;
    }
}
