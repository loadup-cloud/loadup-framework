/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.scheduler.powerjob;

import io.github.loadup.commons.context.ContextHolder;
import io.github.loadup.commons.context.ExecutionContext;
import io.github.loadup.commons.error.CommonException;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

@SuppressWarnings("serial")
public final class PowerJobProcessorAdvisor extends StaticMethodMatcherPointcutAdvisor {
    private final ObservationRegistry observations;

    public PowerJobProcessorAdvisor(ObservationRegistry observations) {
        this.observations = observations;
        setClassFilter(BasicProcessor.class::isAssignableFrom);
        setAdvice((MethodInterceptor) this::process);
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return method.getName().equals("process")
                && method.getParameterCount() == 1
                && method.getParameterTypes()[0] == TaskContext.class;
    }

    private Object process(MethodInvocation invocation) throws Throwable {
        var task = (TaskContext) invocation.getArguments()[0];
        var context = ExecutionContext.empty()
                .with(PowerJobExecutionContext.JOB_ID, task.getJobId())
                .with(PowerJobExecutionContext.INSTANCE_ID, task.getInstanceId())
                .with(PowerJobExecutionContext.TASK_ID, task.getTaskId())
                .with(PowerJobExecutionContext.RETRY_TIMES, task.getCurrentRetryTimes());
        return ContextHolder.callWith(context, () -> {
            Observation observation = Observation.createNotStarted("loadup.powerjob.process", observations)
                    .lowCardinalityKeyValue(
                            "processor",
                            AopUtils.getTargetClass(invocation.getThis()).getName())
                    .start();
            try (var _ = observation.openScope()) {
                ProcessResult result = (ProcessResult) invocation.proceed();
                if (result == null) {
                    throw new CommonException(PowerJobResultCodeEnum.INVALID_PROCESSOR_RESULT);
                }
                observation.lowCardinalityKeyValue("outcome", result.isSuccess() ? "success" : "failure");
                if (!result.isSuccess()) {
                    observation.error(new CommonException(PowerJobResultCodeEnum.PROCESSOR_FAILED));
                }
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
