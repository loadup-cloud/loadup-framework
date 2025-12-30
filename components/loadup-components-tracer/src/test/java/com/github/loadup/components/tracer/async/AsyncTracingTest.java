package com.github.loadup.components.tracer.async;

/*-
 * #%L
 * loadup-components-tracer
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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
        // Call async method
        CompletableFuture<String> future = asyncTestService.asyncOperation("test");
        String result = future.get(5, TimeUnit.SECONDS);

        assertThat(result).contains("test");
        assertThat(result).contains("Async result:");
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
            String traceId = "async-trace";
            try {
                traceId = TraceUtil.getTracerId();
            } catch (Exception e) {
                // Trace context might not be available in async thread
            }
            return CompletableFuture.completedFuture("Async result: " + input + ", traceId: " + traceId);
        }

        @Async
        @Traced(name = "AsyncTestService.tracedAsyncOperation")
        public CompletableFuture<String> tracedAsyncOperation(String input) {
            return CompletableFuture.completedFuture("Processed: " + input);
        }
    }
}

