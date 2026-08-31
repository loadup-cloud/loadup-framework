package io.github.loadup.components.scheduler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collects all {@link SchedulerProcessor} beans and resolves them by task name. Shared by every
 * binder, so the resolution contract is identical regardless of the underlying engine.
 */
public class DefaultSchedulerProcessorRegistry implements SchedulerProcessorRegistry {

    private final Map<String, SchedulerProcessor> processors;

    public DefaultSchedulerProcessorRegistry(List<SchedulerProcessor> processors) {
        this.processors = processors.stream()
                .collect(Collectors.toUnmodifiableMap(
                        SchedulerProcessor::taskName, Function.identity(), (first, duplicate) -> {
                            throw new IllegalStateException(
                                    "Duplicate SchedulerProcessor for taskName '" + duplicate.taskName() + "'");
                        }));
    }

    @Override
    public SchedulerProcessor getProcessor(String taskName) {
        SchedulerProcessor processor = processors.get(taskName);
        if (processor == null) {
            throw new IllegalArgumentException("No SchedulerProcessor registered for taskName '" + taskName + "'");
        }
        return processor;
    }
}
