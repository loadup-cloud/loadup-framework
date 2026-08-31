package io.github.loadup.components.tracer.concurrent;

/*-
 * #%L
 * loadup-components-tracer
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import io.github.loadup.components.tracer.TestConfiguration;
import io.github.loadup.components.tracer.TraceUtil;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * High-concurrency tests verifying TraceContext thread safety.
 */
@SpringBootTest(classes = TestConfiguration.class)
@TestPropertySource(properties = {"spring.application.name=concurrent-test-service", "loadup.tracer.enabled=true"})
class ConcurrentTracingTest {
    private static final Logger log = LoggerFactory.getLogger(ConcurrentTracingTest.class);

    @Autowired
    private Tracer tracer;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        // Fixed thread pool to simulate high concurrency.
        executorService = Executors.newFixedThreadPool(20);
        // Clear the trace context.
        TraceUtil.clearContext();
    }

    @AfterEach
    void tearDown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // Clear the trace context.
        TraceUtil.clearContext();
    }

    /**
     * Verifies concurrent Span creation is thread-safe.
     */
    @Test
    void testConcurrentSpanCreation() throws InterruptedException {
        int threadCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        Set<String> traceIds = Collections.synchronizedSet(new HashSet<>());
        AtomicInteger successCount = new AtomicInteger(0);

        // Start multiple threads creating spans concurrently.
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    // Wait until all threads are ready.
                    startLatch.await();

                    // Create a span.
                    Span span = TraceUtil.createSpan("concurrent-test-" + threadId);
                    assertThat(span).isNotNull();

                    // Read the traceId.
                    String traceId = span.getSpanContext().getTraceId();
                    assertThat(traceId).isNotBlank();
                    traceIds.add(traceId);

                    // Simulate business work.
                    Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));

                    // Verify the span is still recording.
                    assertThat(span.isRecording()).isTrue();

                    // End the span.
                    span.end();

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Thread {} failed", threadId, e);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // Release all threads.
        startLatch.countDown();

        // Wait for all threads to finish.
        boolean finished = endLatch.await(30, TimeUnit.SECONDS);
        assertThat(finished).isTrue();

        // Verify the results.
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(traceIds).hasSize(threadCount); // each thread should have its own traceId

        log.info("Successfully created {} spans across {} threads", successCount.get(), threadCount);
    }

    /**
     * Verifies per-thread TraceContext isolation.
     */
    @Test
    void testTraceContextThreadIsolation() throws InterruptedException, ExecutionException, TimeoutException {
        int threadCount = 50;
        List<Future<String>> futures = new ArrayList<>();

        // Each thread creates its own span and reads its traceId.
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            Future<String> future = executorService.submit(() -> {
                // Create a span.
                Span span = TraceUtil.createSpan("thread-isolation-test-" + threadId);
                String traceId = span.getSpanContext().getTraceId();

                // Verify the span is on the current context.
                Span contextSpan = TraceUtil.getSpan();
                assertThat(contextSpan).isEqualTo(span);

                // Simulate business work.
                Thread.sleep(ThreadLocalRandom.current().nextInt(10, 30));

                // Verify the span was not polluted by other threads.
                Span currentSpan = TraceUtil.getSpan();
                assertThat(currentSpan).isEqualTo(span);
                assertThat(currentSpan.getSpanContext().getTraceId()).isEqualTo(traceId);

                span.end();
                TraceUtil.clearContext();

                return traceId;
            });
            futures.add(future);
        }

        // Collect all traceIds.
        Set<String> traceIds = new HashSet<>();
        for (Future<String> future : futures) {
            String traceId = future.get(10, TimeUnit.SECONDS);
            assertThat(traceId).isNotBlank();
            traceIds.add(traceId);
        }

        // Verify each thread has a distinct traceId.
        assertThat(traceIds).hasSize(threadCount);

        log.info("Verified thread isolation across {} threads", threadCount);
    }

    /**
     * Verifies nested spans under concurrency.
     */
    @Test
    void testConcurrentNestedSpans() throws InterruptedException {
        int threadCount = 30;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    // Create the parent span.
                    Span parentSpan = TraceUtil.createSpan("parent-" + threadId);
                    String parentTraceId = parentSpan.getSpanContext().getTraceId();

                    // Create a child span.
                    Span childSpan1 = tracer.spanBuilder("child1-" + threadId)
                            .setParent(
                                    io.opentelemetry.context.Context.current().with(parentSpan))
                            .startSpan();

                    // Verify the child inherits the parent traceId.
                    assertThat(childSpan1.getSpanContext().getTraceId()).isEqualTo(parentTraceId);

                    Thread.sleep(ThreadLocalRandom.current().nextInt(5, 20));

                    // Create a second child span.
                    Span childSpan2 = tracer.spanBuilder("child2-" + threadId)
                            .setParent(
                                    io.opentelemetry.context.Context.current().with(parentSpan))
                            .startSpan();

                    assertThat(childSpan2.getSpanContext().getTraceId()).isEqualTo(parentTraceId);

                    // End all spans.
                    childSpan2.end();
                    childSpan1.end();
                    parentSpan.end();

                    TraceUtil.clearContext();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Thread {} failed", threadId, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finished = latch.await(30, TimeUnit.SECONDS);
        assertThat(finished).isTrue();
        assertThat(successCount.get()).isEqualTo(threadCount);

        log.info("Successfully tested nested spans across {} threads", threadCount);
    }

    /**
     * Verifies span creation/destruction under high load.
     */
    @Test
    void testHighLoadSpanCreation() throws InterruptedException {
        int totalSpans = 1000;
        CountDownLatch latch = new CountDownLatch(totalSpans);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // Create many spans quickly.
        for (int i = 0; i < totalSpans; i++) {
            final int spanId = i;
            executorService.submit(() -> {
                try {
                    Span span = TraceUtil.createSpan("high-load-" + spanId);

                    // Add attributes.
                    span.setAttribute("span.id", spanId);
                    span.setAttribute("test.type", "high-load");

                    // Brief delay.
                    Thread.sleep(1);

                    span.end();
                    TraceUtil.clearContext();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.error("Failed to create span {}", spanId, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finished = latch.await(60, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;

        assertThat(finished).isTrue();
        assertThat(successCount.get()).isEqualTo(totalSpans);
        assertThat(errorCount.get()).isEqualTo(0);

        double throughput = (double) totalSpans / duration * 1000;
        log.info(
                "Created {} spans in {}ms, throughput: {} spans/second",
                totalSpans,
                duration,
                String.format("%.2f", throughput));
    }

    /**
     * Verifies thread-safe context cleanup.
     */
    @Test
    void testConcurrentContextCleanup() throws InterruptedException {
        int threadCount = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // Create a span.
                    Span span = TraceUtil.createSpan("cleanup-test");
                    assertThat(TraceUtil.getSpan()).isEqualTo(span);

                    Thread.sleep(ThreadLocalRandom.current().nextInt(5, 15));

                    // Clean up.
                    span.end();
                    TraceUtil.clearContext();

                    // Verify the context is empty.
                    assertThat(TraceUtil.getSpan()).isNull();

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Cleanup test failed", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finished = latch.await(30, TimeUnit.SECONDS);
        assertThat(finished).isTrue();
        assertThat(successCount.get()).isEqualTo(threadCount);

        log.info("Successfully tested context cleanup across {} threads", threadCount);
    }

    /**
     * Verifies concurrent traceId reads.
     */
    @Test
    void testConcurrentGetTraceId() throws InterruptedException {
        int threadCount = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Map<Integer, String> threadTraceIds = new ConcurrentHashMap<>();
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    // Wait until all threads are ready.
                    startLatch.await();

                    // Create a span.
                    Span span = TraceUtil.createSpan("get-traceid-test-" + threadId);

                    // Read the traceId.
                    String traceId = TraceUtil.getTracerId();
                    assertThat(traceId).isNotBlank();

                    threadTraceIds.put(threadId, traceId);

                    Thread.sleep(10);

                    // A second read must return the same value.
                    String traceId2 = TraceUtil.getTracerId();
                    assertThat(traceId2).isEqualTo(traceId);

                    span.end();
                    TraceUtil.clearContext();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Thread {} failed", threadId, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Release all threads.
        startLatch.countDown();

        boolean finished = latch.await(30, TimeUnit.SECONDS);
        assertThat(finished).isTrue();
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(threadTraceIds).hasSize(threadCount);

        // Verify all traceIds are unique.
        Set<String> uniqueTraceIds = new HashSet<>(threadTraceIds.values());
        assertThat(uniqueTraceIds).hasSize(threadCount);

        log.info("Successfully verified traceId uniqueness across {} threads", threadCount);
    }

    /**
     * Stress test simulating a real high-concurrency workload.
     */
    @Test
    void testRealWorldHighConcurrency() throws InterruptedException {
        int requestCount = 200;
        CountDownLatch latch = new CountDownLatch(requestCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<Long> latencies = Collections.synchronizedList(new ArrayList<>());

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < requestCount; i++) {
            final int requestId = i;
            executorService.submit(() -> {
                long requestStart = System.nanoTime();
                try {
                    // Simulate an HTTP request.
                    Span requestSpan = TraceUtil.createSpan("http.request." + requestId);
                    requestSpan.setAttribute("http.method", "GET");
                    requestSpan.setAttribute("http.url", "/api/test/" + requestId);

                    // Simulate a database query.
                    Span dbSpan = tracer.spanBuilder("db.query")
                            .setParent(
                                    io.opentelemetry.context.Context.current().with(requestSpan))
                            .startSpan();
                    Thread.sleep(ThreadLocalRandom.current().nextInt(5, 15));
                    dbSpan.end();

                    // Simulate a cache read.
                    Span cacheSpan = tracer.spanBuilder("cache.get")
                            .setParent(
                                    io.opentelemetry.context.Context.current().with(requestSpan))
                            .startSpan();
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1, 5));
                    cacheSpan.end();

                    // Simulate business work.
                    Thread.sleep(ThreadLocalRandom.current().nextInt(10, 30));

                    requestSpan.setAttribute("http.status", 200);
                    requestSpan.end();

                    TraceUtil.clearContext();

                    long requestEnd = System.nanoTime();
                    latencies.add((requestEnd - requestStart) / 1_000_000); // Convert to ms

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.error("Request {} failed", requestId, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finished = latch.await(60, TimeUnit.SECONDS);
        long totalDuration = System.currentTimeMillis() - startTime;

        assertThat(finished).isTrue();
        assertThat(successCount.get()).isEqualTo(requestCount);
        assertThat(errorCount.get()).isEqualTo(0);

        // Compute statistics.
        double avgLatency =
                latencies.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxLatency = latencies.stream().mapToLong(Long::longValue).max().orElse(0);
        long minLatency = latencies.stream().mapToLong(Long::longValue).min().orElse(0);
        double throughput = (double) requestCount / totalDuration * 1000;

        log.info("Real-world concurrency test results:");
        log.info("  Total requests: {}", requestCount);
        log.info("  Success: {}", successCount.get());
        log.info("  Errors: {}", errorCount.get());
        log.info("  Total duration: {}ms", totalDuration);
        log.info("  Throughput: {} req/s", String.format("%.2f", throughput));
        log.info(
                "  Latency - Avg: {}ms, Min: {}ms, Max: {}ms",
                String.format("%.2f", avgLatency),
                minLatency,
                maxLatency);
    }
}
