package io.github.loadup.retrytask.jobrunr;

import io.github.loadup.retrytask.facade.RetryTaskProcessor;
import io.github.loadup.retrytask.facade.RetryTaskProcessorRegistry;
import io.github.loadup.retrytask.facade.model.RetryTaskContext;
import org.jobrunr.jobs.lambdas.JobRequestHandler;

/**
 * Dispatches a {@link RetryTaskJobRequest} to the {@link RetryTaskProcessor} registered for its
 * business type. A processor exception propagates and triggers the JobRunr retry policy.
 */
public class RetryTaskJobRequestHandler implements JobRequestHandler<RetryTaskJobRequest> {

    private final RetryTaskProcessorRegistry processorRegistry;

    public RetryTaskJobRequestHandler(RetryTaskProcessorRegistry processorRegistry) {
        this.processorRegistry = processorRegistry;
    }

    @Override
    public void run(RetryTaskJobRequest jobRequest) throws Exception {
        RetryTaskProcessor processor = processorRegistry.getProcessor(jobRequest.getBizType());
        processor.process(new RetryTaskContext(jobRequest.getBizType(), jobRequest.getBizId(), jobRequest.getArgs()));
    }
}
