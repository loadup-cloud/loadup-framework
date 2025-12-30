package com.github.loadup.components.tracer.concurrent;

/*-
 * #%L
 * loadup-components-tracer
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import com.github.loadup.components.tracer.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 高并发场景测试 - 测试 TraceContext 在多线程环境下的线程安全性
 */
@Slf4j
@SpringBootTest(classes = TestConfiguration.class)
@TestPropertySource(properties = {
    "spring.application.name=concurrent-test-service",
    "loadup.tracer.enabled=true"
})
class ConcurrentTracingTest {

    @Autowired
    private Tracer tracer;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        // 使用固定线程池，模拟高并发场景
        executorService = Executors.newFixedThreadPool(20);
        // 清理 trace context
        TraceUtil.getTraceContext().clear();
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
        // 清理 trace context
        TraceUtil.getTraceContext().clear();
    }

    /**
     * 测试多线程并发创建 Span 的线程安全性
     */
    @Test
    void testConcurrentSpanCreation() throws InterruptedException {
        int threadCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        Set<String> traceIds = Collections.synchronizedSet(new HashSet<>());
        AtomicInteger successCount = new AtomicInteger(0);

        // 启动多个线程并发创建 Span
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    // 等待所有线程就绪
                    startLatch.await();

                    // 创建 Span
                    Span span = TraceUtil.createSpan("concurrent-test-" + threadId);
                    assertThat(span).isNotNull();

                    // 获取 traceId
                    String traceId = span.getSpanContext().getTraceId();
                    assertThat(traceId).isNotBlank();
                    traceIds.add(traceId);

                    // 模拟业务处理
                    Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));

                    // 验证 Span 仍然有效
                    assertThat(span.isRecording()).isTrue();

                    // 结束 Span
                    span.end();

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Thread {} failed", threadId, e);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 启动所有线程
        startLatch.countDown();

        // 等待所有线程完成
        boolean finished = endLatch.await(30, TimeUnit.SECONDS);
        assertThat(finished).isTrue();

        // 验证结果
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(traceIds).hasSize(threadCount); // 每个线程应该有不同的 traceId

        log.info("Successfully created {} spans across {} threads", successCount.get(), threadCount);
    }

    /**
     * 测试 TraceContext 的线程隔离性
     */
    @Test
    void testTraceContextThreadIsolation() throws InterruptedException, ExecutionException, TimeoutException {
        int threadCount = 50;
        List<Future<String>> futures = new ArrayList<>();

        // 每个线程创建自己的 Span 并获取 traceId
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            Future<String> future = executorService.submit(() -> {
                // 创建 Span
                Span span = TraceUtil.createSpan("thread-isolation-test-" + threadId);
                String traceId = span.getSpanContext().getTraceId();

                // 验证 TraceContext 中的 Span
                TraceContext context = TraceUtil.getTraceContext();
                Span contextSpan = context.getCurrentSpan();
                assertThat(contextSpan).isEqualTo(span);

                // 模拟业务处理
                Thread.sleep(ThreadLocalRandom.current().nextInt(10, 30));

                // 再次验证 Span 没有被其他线程污染
                Span currentSpan = context.getCurrentSpan();
                assertThat(currentSpan).isEqualTo(span);
                assertThat(currentSpan.getSpanContext().getTraceId()).isEqualTo(traceId);

                span.end();
                context.clear();

                return traceId;
            });
            futures.add(future);
        }

        // 收集所有 traceId
        Set<String> traceIds = new HashSet<>();
        for (Future<String> future : futures) {
            String traceId = future.get(10, TimeUnit.SECONDS);
            assertThat(traceId).isNotBlank();
            traceIds.add(traceId);
        }

        // 验证每个线程都有独立的 traceId
        assertThat(traceIds).hasSize(threadCount);

        log.info("Verified thread isolation across {} threads", threadCount);
    }

    /**
     * 测试嵌套 Span 的并发处理
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
                    // 创建父 Span
                    Span parentSpan = TraceUtil.createSpan("parent-" + threadId);
                    String parentTraceId = parentSpan.getSpanContext().getTraceId();

                    // 创建子 Span
                    Span childSpan1 = tracer.spanBuilder("child1-" + threadId)
                        .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
                        .startSpan();

                    // 验证子 Span 继承了父 Span 的 traceId
                    assertThat(childSpan1.getSpanContext().getTraceId()).isEqualTo(parentTraceId);

                    Thread.sleep(ThreadLocalRandom.current().nextInt(5, 20));

                    // 创建第二个子 Span
                    Span childSpan2 = tracer.spanBuilder("child2-" + threadId)
                        .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
                        .startSpan();

                    assertThat(childSpan2.getSpanContext().getTraceId()).isEqualTo(parentTraceId);

                    // 结束所有 Span
                    childSpan2.end();
                    childSpan1.end();
                    parentSpan.end();

                    TraceUtil.getTraceContext().clear();
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
     * 测试高负载下的 Span 创建和销毁
     */
    @Test
    void testHighLoadSpanCreation() throws InterruptedException {
        int totalSpans = 1000;
        CountDownLatch latch = new CountDownLatch(totalSpans);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // 快速创建大量 Span
        for (int i = 0; i < totalSpans; i++) {
            final int spanId = i;
            executorService.submit(() -> {
                try {
                    Span span = TraceUtil.createSpan("high-load-" + spanId);

                    // 添加属性
                    span.setAttribute("span.id", spanId);
                    span.setAttribute("test.type", "high-load");

                    // 短暂延迟
                    Thread.sleep(1);

                    span.end();
                    TraceUtil.getTraceContext().clear();
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
        log.info("Created {} spans in {}ms, throughput: {} spans/second",
            totalSpans, duration, String.format("%.2f", throughput));
    }

    /**
     * 测试 TraceContext 清理的线程安全性
     */
    @Test
    void testConcurrentContextCleanup() throws InterruptedException {
        int threadCount = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    TraceContext context = TraceUtil.getTraceContext();

                    // 创建 Span
                    Span span = TraceUtil.createSpan("cleanup-test");
                    assertThat(context.isEmpty()).isFalse();

                    Thread.sleep(ThreadLocalRandom.current().nextInt(5, 15));

                    // 清理
                    span.end();
                    context.clear();

                    // 验证已清理
                    assertThat(context.isEmpty()).isTrue();

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
     * 测试并发获取 TraceId
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
                    // 等待所有线程就绪
                    startLatch.await();

                    // 创建 Span
                    Span span = TraceUtil.createSpan("get-traceid-test-" + threadId);

                    // 获取 traceId
                    String traceId = TraceUtil.getTracerId();
                    assertThat(traceId).isNotBlank();

                    threadTraceIds.put(threadId, traceId);

                    Thread.sleep(10);

                    // 再次获取，应该相同
                    String traceId2 = TraceUtil.getTracerId();
                    assertThat(traceId2).isEqualTo(traceId);

                    span.end();
                    TraceUtil.getTraceContext().clear();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Thread {} failed", threadId, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 启动所有线程
        startLatch.countDown();

        boolean finished = latch.await(30, TimeUnit.SECONDS);
        assertThat(finished).isTrue();
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(threadTraceIds).hasSize(threadCount);

        // 验证所有 traceId 都是唯一的
        Set<String> uniqueTraceIds = new HashSet<>(threadTraceIds.values());
        assertThat(uniqueTraceIds).hasSize(threadCount);

        log.info("Successfully verified traceId uniqueness across {} threads", threadCount);
    }

    /**
     * 压力测试 - 模拟真实的高并发场景
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
                    // 模拟 HTTP 请求处理
                    Span requestSpan = TraceUtil.createSpan("http.request." + requestId);
                    requestSpan.setAttribute("http.method", "GET");
                    requestSpan.setAttribute("http.url", "/api/test/" + requestId);

                    // 模拟数据库查询
                    Span dbSpan = tracer.spanBuilder("db.query")
                        .setParent(io.opentelemetry.context.Context.current().with(requestSpan))
                        .startSpan();
                    Thread.sleep(ThreadLocalRandom.current().nextInt(5, 15));
                    dbSpan.end();

                    // 模拟缓存操作
                    Span cacheSpan = tracer.spanBuilder("cache.get")
                        .setParent(io.opentelemetry.context.Context.current().with(requestSpan))
                        .startSpan();
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1, 5));
                    cacheSpan.end();

                    // 模拟业务处理
                    Thread.sleep(ThreadLocalRandom.current().nextInt(10, 30));

                    requestSpan.setAttribute("http.status", 200);
                    requestSpan.end();

                    TraceUtil.getTraceContext().clear();

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

        // 计算统计信息
        double avgLatency = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxLatency = latencies.stream().mapToLong(Long::longValue).max().orElse(0);
        long minLatency = latencies.stream().mapToLong(Long::longValue).min().orElse(0);
        double throughput = (double) requestCount / totalDuration * 1000;

        log.info("Real-world concurrency test results:");
        log.info("  Total requests: {}", requestCount);
        log.info("  Success: {}", successCount.get());
        log.info("  Errors: {}", errorCount.get());
        log.info("  Total duration: {}ms", totalDuration);
        log.info("  Throughput: {} req/s", String.format("%.2f", throughput));
        log.info("  Latency - Avg: {}ms, Min: {}ms, Max: {}ms",
            String.format("%.2f", avgLatency), minLatency, maxLatency);
    }
}

