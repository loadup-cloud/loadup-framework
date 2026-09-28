package io.github.loadup.common.tracer.async;

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
