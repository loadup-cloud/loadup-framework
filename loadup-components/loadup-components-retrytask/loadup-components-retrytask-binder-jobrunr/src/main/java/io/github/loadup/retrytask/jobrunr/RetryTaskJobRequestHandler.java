/*-
 * #%L
 * Loadup Components Retrytask Binder JobRunr
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
