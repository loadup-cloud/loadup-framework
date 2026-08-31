package io.github.loadup.components.scheduler.jobrunr;

import io.github.loadup.components.scheduler.SchedulerProcessor;
import io.github.loadup.components.scheduler.SchedulerProcessorRegistry;
import io.github.loadup.components.scheduler.model.SchedulerContext;
import org.jobrunr.jobs.lambdas.JobRequestHandler;

/**
 * Dispatches a {@link SchedulerJobRequest} to the {@link SchedulerProcessor} registered for its
 * task name. A processor exception propagates and triggers the JobRunr retry policy — the same
 * failure semantics as the retry task component.
 */
public class SchedulerJobRequestHandler implements JobRequestHandler<SchedulerJobRequest> {

    private final SchedulerProcessorRegistry processorRegistry;

    public SchedulerJobRequestHandler(SchedulerProcessorRegistry processorRegistry) {
        this.processorRegistry = processorRegistry;
    }

    @Override
    public void run(SchedulerJobRequest jobRequest) throws Exception {
        SchedulerProcessor processor = processorRegistry.getProcessor(jobRequest.getTaskName());
        processor.process(new SchedulerContext(jobRequest.getTaskName(), jobRequest.getArgs()));
    }
}
