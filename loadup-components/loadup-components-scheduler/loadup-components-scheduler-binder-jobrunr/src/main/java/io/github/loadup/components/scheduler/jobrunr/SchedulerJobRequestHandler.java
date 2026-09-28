package io.github.loadup.components.scheduler.jobrunr;

import io.github.loadup.components.scheduler.SchedulerProcessor;
import io.github.loadup.components.scheduler.SchedulerProcessorRegistry;
import io.github.loadup.components.scheduler.model.SchedulerContext;
import org.jobrunr.jobs.lambdas.JobRequestHandler;

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
