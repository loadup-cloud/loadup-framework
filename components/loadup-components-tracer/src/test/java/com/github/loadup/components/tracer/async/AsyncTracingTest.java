package com.github.loadup.components.tracer.async;

import com.github.loadup.components.tracer.TestConfiguration;
import com.github.loadup.components.tracer.TraceUtil;
import com.github.loadup.components.tracer.annotation.Traced;
import io.opentelemetry.api.trace.Span;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for async tracing functionality.
 */
@SpringBootTest(classes = {TestConfiguration.class, AsyncTracingTest.AsyncTestService.class})
@TestPropertySource(properties = {
    "spring.application.name=async-test-service",
    "loadup.tracer.enabled=true",
    "loadup.tracer.enable-async-tracing=true"
})
class AsyncTracingTest {

    @Autowired
    private AsyncTestService asyncTestService;

    @Test
    void testAsyncMethodTracing() throws ExecutionException, InterruptedException, TimeoutException {
        // Create a parent span
        Span parentSpan = TraceUtil.createSpan("parent-operation");
        String parentTraceId = parentSpan.getSpanContext().getTraceId();

        try {
            // Call async method
            CompletableFuture<String> future = asyncTestService.asyncOperation("test");
            String result = future.get(5, TimeUnit.SECONDS);

            assertThat(result).contains("test");
            assertThat(result).contains(parentTraceId); // Should have same trace context
        } finally {
            parentSpan.end();
        }
    }

    @Test
    void testAsyncMethodWithTraced() throws ExecutionException, InterruptedException, TimeoutException {
        Span parentSpan = TraceUtil.createSpan("parent-with-traced");

        try {
            CompletableFuture<String> future = asyncTestService.tracedAsyncOperation("data");
            String result = future.get(5, TimeUnit.SECONDS);

            assertThat(result).isEqualTo("Processed: data");
        } finally {
            parentSpan.end();
        }
    }

    @Service
    static class AsyncTestService {

        @Async
        public CompletableFuture<String> asyncOperation(String input) {
            // Get current trace context in async thread
            String traceId = TraceUtil.getTracerId();
            return CompletableFuture.completedFuture("Async result: " + input + ", traceId: " + traceId);
        }

        @Async
        @Traced(name = "AsyncTestService.tracedAsyncOperation")
        public CompletableFuture<String> tracedAsyncOperation(String input) {
            return CompletableFuture.completedFuture("Processed: " + input);
        }
    }
}

