package io.github.loadup.common.tracer.async;

/*-
 * #%L
 * Loadup Common Tracer
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

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

/**
 * {@link TaskDecorator} that propagates the OpenTelemetry {@link Context} and SLF4J MDC
 * values from the submitting thread into the executing thread of an async task.
 *
 * <p>Without this decorator, async tasks (e.g. {@code @Async} methods, virtual threads)
 * start with an empty context, breaking distributed trace chains and losing log
 * correlation fields like {@code traceId} / {@code spanId}.
 *
 * <p>Register this bean and wire it into any {@code ThreadPoolTaskExecutor} or
 * {@code SimpleAsyncTaskExecutor} used in your application.
 */
public class TracingTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // Capture caller-thread state before the task is submitted.
        Context callerContext = Context.current();
        Map<String, String> callerMdc = MDC.getCopyOfContextMap();

        return () -> {
            // Restore caller context in the worker thread.
            Scope scope = callerContext.makeCurrent();
            try {
                if (callerMdc != null) {
                    MDC.setContextMap(callerMdc);
                } else {
                    MDC.clear();
                }
                runnable.run();
            } finally {
                scope.close();
                MDC.clear();
            }
        };
    }
}
