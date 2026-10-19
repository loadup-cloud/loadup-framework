/*
 * #%L
 * LoadUp Components Scheduler Binder PowerJob
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.components.scheduler.powerjob;

import io.github.loadup.commons.context.ContextHolder;
import io.github.loadup.commons.context.ExecutionContext;
import io.github.loadup.commons.error.CommonException;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;

/** Instruments Spring processors while retaining native PowerJob result and exception semantics. */
@Aspect
public class PowerJobProcessorAspect {
    private final ObservationRegistry observations;

    public PowerJobProcessorAspect(ObservationRegistry observations) {
        this.observations = observations;
    }

    @Around("target(tech.powerjob.worker.core.processor.sdk.BasicProcessor)"
            + " && execution(* process(tech.powerjob.worker.core.processor.TaskContext)) && args(task)")
    public Object process(ProceedingJoinPoint invocation, TaskContext task) throws Throwable {
        var context = ExecutionContext.empty()
                .with(PowerJobExecutionContext.JOB_ID, task.getJobId())
                .with(PowerJobExecutionContext.INSTANCE_ID, task.getInstanceId())
                .with(PowerJobExecutionContext.TASK_ID, task.getTaskId())
                .with(PowerJobExecutionContext.RETRY_TIMES, task.getCurrentRetryTimes());
        return ContextHolder.callWith(context, () -> {
            Observation observation = Observation.createNotStarted("loadup.powerjob.process", observations)
                    .lowCardinalityKeyValue(
                            "processor",
                            org.springframework.aop.support.AopUtils.getTargetClass(invocation.getTarget())
                                    .getName())
                    .start();
            try (var _ = observation.openScope()) {
                ProcessResult result = (ProcessResult) invocation.proceed();
                if (result == null) throw new CommonException(PowerJobResultCodeEnum.INVALID_PROCESSOR_RESULT);
                observation.lowCardinalityKeyValue("outcome", result.isSuccess() ? "success" : "failure");
                if (!result.isSuccess())
                    observation.error(new CommonException(PowerJobResultCodeEnum.PROCESSOR_FAILED));
                return result;
            } catch (Throwable failure) {
                observation.lowCardinalityKeyValue("outcome", "failure");
                // Exception messages and chains may include processor payloads.
                observation.error(new CommonException(PowerJobResultCodeEnum.PROCESSOR_FAILED));
                throw failure;
            } finally {
                observation.stop();
            }
        });
    }
}
