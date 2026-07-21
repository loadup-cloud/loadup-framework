package io.github.loadup.components.tracer.async;

/*-
 * #%L
 * Loadup Components Tracer
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

import io.opentelemetry.context.Context;
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
            try (io.opentelemetry.context.Scope ignored = callerContext.makeCurrent()) {
                if (callerMdc != null) {
                    MDC.setContextMap(callerMdc);
                } else {
                    MDC.clear();
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
