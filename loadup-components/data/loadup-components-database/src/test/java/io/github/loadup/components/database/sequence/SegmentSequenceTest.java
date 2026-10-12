/*
 * #%L
 * Loadup Components Database
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
package io.github.loadup.components.database.sequence;

import static org.assertj.core.api.Assertions.*;

import io.github.loadup.commons.error.CommonException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.Test;

class SegmentSequenceTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-12T00:00:00Z"), ZoneOffset.UTC);

    private SequenceScope scope(String name, long maximum) {
        return new SequenceScope(name, LocalDate.of(2026, 10, 12), maximum);
    }

    private SequenceProperties properties(int step) {
        SequenceProperties p = new SequenceProperties();
        p.setStep(step);
        return p;
    }

    private static final class Store implements SequenceRangeAllocator {
        private final Map<String, Long> maxima = new HashMap<>();
        private final AtomicInteger calls = new AtomicInteger();

        public synchronized SequenceRange reserve(SequenceScope scope, int step) {
            calls.incrementAndGet();
            long old = maxima.getOrDefault(scope.key(), 0L);
            if (old >= scope.maximum()) throw new CommonException(DatabaseResultCodeEnum.EXHAUSTED);
            long last = old + Math.min(step, scope.maximum() - old);
            maxima.put(scope.key(), last);
            return new SequenceRange(old + 1, last);
        }
    }

    @Test
    void concurrentVirtualThreadsAndBatchesRemainUnique() throws Exception {
        Store store = new Store();
        Set<Long> seen = ConcurrentHashMap.newKeySet();
        SequenceScope scope = scope("hot", 1_000_000);
        try (SegmentSequence sequence = new SegmentSequence(store, properties(1000), clock);
                ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> tasks = new ArrayList<>();
            for (int t = 0; t < 32; t++)
                tasks.add(workers.submit(() -> {
                    for (int n = 0; n < 100; n++) {
                        for (long id : sequence.nextBatch(scope, 37))
                            assertThat(seen.add(id)).isTrue();
                        assertThat(seen.add(sequence.next(scope))).isTrue();
                    }
                }));
            for (Future<?> task : tasks) task.get(20, TimeUnit.SECONDS);
            assertThat(seen).hasSize(121600);
            assertThat(store.calls.get()).isLessThan(130);
        }
    }

    @Test
    void clientsShareCommittedRangesAndRestartDoesNotReuse() {
        Store store = new Store();
        SequenceScope scope = scope("shared", 1000);
        Set<Long> seen = new HashSet<>();
        try (SegmentSequence a = new SegmentSequence(store, properties(10), clock);
                SegmentSequence b = new SegmentSequence(store, properties(10), clock)) {
            for (long id : a.nextBatch(scope, 27)) assertThat(seen.add(id)).isTrue();
            for (long id : b.nextBatch(scope, 27)) assertThat(seen.add(id)).isTrue();
        }
        try (SegmentSequence restarted = new SegmentSequence(store, properties(10), clock)) {
            for (long id : restarted.nextBatch(scope, 27))
                assertThat(seen.add(id)).isTrue();
        }
    }

    @Test
    void clampsFinalRangeAndRejectsExhaustion() {
        Store store = new Store();
        SequenceScope scope = scope("small", 23);
        try (SegmentSequence sequence = new SegmentSequence(store, properties(10), clock)) {
            assertThat(sequence.nextBatch(scope, 23))
                    .containsExactly(
                            java.util.stream.LongStream.rangeClosed(1, 23).toArray());
            assertThatThrownBy(() -> sequence.next(scope))
                    .isInstanceOfSatisfying(
                            CommonException.class,
                            e -> assertThat(e.getResultCode()).isEqualTo(DatabaseResultCodeEnum.EXHAUSTED));
        }
    }

    @Test
    void scopeLimitsAndConflictingMaximumFail() {
        SequenceProperties p = properties(10);
        p.setMaxScopes(1);
        try (SegmentSequence sequence = new SegmentSequence(new Store(), p, clock)) {
            sequence.next(scope("one", 100));
            assertThatThrownBy(() -> sequence.next(scope("one", 101)))
                    .isInstanceOfSatisfying(
                            CommonException.class,
                            e -> assertThat(e.getResultCode()).isEqualTo(DatabaseResultCodeEnum.CONFIG_CONFLICT));
            assertThatThrownBy(() -> sequence.next(scope("two", 100)))
                    .isInstanceOfSatisfying(
                            CommonException.class,
                            e -> assertThat(e.getResultCode()).isEqualTo(DatabaseResultCodeEnum.SCOPE_LIMIT));
        }
    }

    @Test
    void expiredScopesYieldCacheCapacityWithoutReusingRanges() {
        SequenceProperties p = properties(10);
        p.setMaxScopes(1);
        Set<Long> old = new HashSet<>();
        SequenceScope yesterday = new SequenceScope("day", LocalDate.of(2026, 10, 11), 1000);
        try (SegmentSequence sequence = new SegmentSequence(new Store(), p, clock)) {
            for (long n : sequence.nextBatch(yesterday, 3)) old.add(n);
            sequence.next(scope("day", 1000));
            assertThatThrownBy(() -> sequence.next(yesterday)).isInstanceOf(CommonException.class);
        }
    }

    @Test
    void timeoutDoesNotCancelCommittedAllocation() throws Exception {
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        Store store = new Store();
        SequenceProperties p = properties(10);
        p.setWaitTimeout(Duration.ofMillis(25));
        try (SegmentSequence sequence = new SegmentSequence(
                (scope, step) -> {
                    entered.countDown();
                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new CommonException(DatabaseResultCodeEnum.UNAVAILABLE, e);
                    }
                    return store.reserve(scope, step);
                },
                p,
                clock)) {
            assertThatThrownBy(() -> sequence.next(scope("slow", 100)))
                    .isInstanceOfSatisfying(
                            CommonException.class,
                            e -> assertThat(e.getResultCode()).isEqualTo(DatabaseResultCodeEnum.TIMEOUT));
            assertThat(entered.await(1, TimeUnit.SECONDS)).isTrue();
            release.countDown();
            assertThat(sequence.next(scope("slow", 100))).isEqualTo(1);
        }
    }

    @Test
    void failedPrefetchPreservesActiveNumbers() {
        AtomicInteger attempts = new AtomicInteger();
        Store store = new Store();
        try (SegmentSequence sequence = new SegmentSequence(
                (scope, step) -> {
                    if (attempts.incrementAndGet() > 1) throw new CommonException(DatabaseResultCodeEnum.UNAVAILABLE);
                    return store.reserve(scope, step);
                },
                properties(10),
                clock)) {
            assertThat(sequence.nextBatch(scope("fail", 100), 8)).hasSize(8);
            assertThat(sequence.nextBatch(scope("fail", 100), 2)).containsExactly(9, 10);
            assertThatThrownBy(() -> sequence.next(scope("fail", 100))).isInstanceOf(CommonException.class);
        }
    }

    @Test
    void validatesBatchAndCloseAndCollectsMetrics() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        SegmentSequence sequence = new SegmentSequence(new Store(), properties(100), clock, registry);
        assertThat(sequence.nextBatch(scope("metric", 100), 12)).hasSize(12);
        assertThat(registry.get("loadup.database.sequence.issued").counter().count())
                .isEqualTo(12);
        assertThatThrownBy(() -> sequence.nextBatch(scope("metric", 100), 0)).isInstanceOf(CommonException.class);
        sequence.close();
        assertThatThrownBy(() -> sequence.next(scope("metric", 100)))
                .isInstanceOfSatisfying(
                        CommonException.class,
                        e -> assertThat(e.getResultCode()).isEqualTo(DatabaseResultCodeEnum.CLOSED));
        registry.close();
    }

    @Test
    void boundedQueueRejectsAdditionalColdScopes() throws Exception {
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        SequenceProperties p = properties(10);
        p.setPrefetchThreads(1);
        p.setQueueCapacity(1);
        p.setWaitTimeout(Duration.ofMillis(30));
        try (SegmentSequence sequence = new SegmentSequence(
                (scope, step) -> {
                    entered.countDown();
                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new CommonException(DatabaseResultCodeEnum.UNAVAILABLE, e);
                    }
                    return new SequenceRange(1, 10);
                },
                p,
                clock)) {
            assertThatThrownBy(() -> sequence.next(scope("running", 100))).isInstanceOf(CommonException.class);
            assertThat(entered.await(1, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> sequence.next(scope("queued", 100))).isInstanceOf(CommonException.class);
            assertThatThrownBy(() -> sequence.next(scope("rejected", 100)))
                    .isInstanceOfSatisfying(
                            CommonException.class,
                            e -> assertThat(e.getResultCode()).isEqualTo(DatabaseResultCodeEnum.UNAVAILABLE));
        } finally {
            release.countDown();
        }
    }

    @Test
    void closeReleasesOutstandingWait() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        SequenceProperties p = properties(10);
        p.setWaitTimeout(Duration.ofSeconds(10));
        SegmentSequence sequence = new SegmentSequence(
                (scope, step) -> {
                    entered.countDown();
                    try {
                        new CountDownLatch(1).await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new CommonException(DatabaseResultCodeEnum.UNAVAILABLE, e);
                    }
                    return new SequenceRange(1, 10);
                },
                p,
                clock);
        try (ExecutorService caller = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> waiting = caller.submit(() -> assertThatThrownBy(() -> sequence.next(scope("close", 100)))
                    .isInstanceOfSatisfying(
                            CommonException.class,
                            e -> assertThat(e.getResultCode()).isEqualTo(DatabaseResultCodeEnum.CLOSED)));
            assertThat(entered.await(1, TimeUnit.SECONDS)).isTrue();
            sequence.close();
            waiting.get(2, TimeUnit.SECONDS);
        } finally {
            sequence.close();
        }
    }
}
