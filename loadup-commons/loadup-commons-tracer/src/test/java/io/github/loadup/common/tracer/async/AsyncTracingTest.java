package io.github.loadup.common.tracer.async;

/*-
 * #%L
 * loadup-commons-tracer
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.common.tracer.TestConfiguration;
import io.github.loadup.common.tracer.TraceUtil;
import io.github.loadup.common.tracer.annotation.Traced;
import io.opentelemetry.api.trace.Span;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for async tracing functionality.
 */
@SpringBootTest(classes = {TestConfiguration.class, AsyncTracingTest.AsyncTestService.class})
@TestPropertySource(
        properties = {
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
