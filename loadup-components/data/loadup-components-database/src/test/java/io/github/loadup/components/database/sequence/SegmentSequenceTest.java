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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.commons.error.CommonException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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

    @Test
    void concurrentVirtualThreadsAndBatchesRemainUnique() throws Exception {
        Store store = new Store();
        Set<Long> seen = ConcurrentHashMap.newKeySet();
        SequenceScope scope = scope("hot", 1_000_000);
        try (SegmentSequence sequence = new SegmentSequence(store, properties(1000), clock);
                ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> tasks = new ArrayList<>();
            for (int t = 0; t < 32; t++) {
                tasks.add(workers.submit(() -> {
                    for (int n = 0; n < 100; n++) {
                        for (long id : sequence.nextBatch(scope, 37)) {
                            assertThat(seen.add(id)).isTrue();
                        }
                        assertThat(seen.add(sequence.next(scope))).isTrue();
                    }
                }));
            }
            for (Future<?> task : tasks) {
                task.get(20, TimeUnit.SECONDS);
            }
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
            for (long id : a.nextBatch(scope, 27)) {
                assertThat(seen.add(id)).isTrue();
            }
            for (long id : b.nextBatch(scope, 27)) {
                assertThat(seen.add(id)).isTrue();
            }
        }
        try (SegmentSequence restarted = new SegmentSequence(store, properties(10), clock)) {
            for (long id : restarted.nextBatch(scope, 27)) {
                assertThat(seen.add(id)).isTrue();
            }
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
            for (long n : sequence.nextBatch(yesterday, 3)) {
                old.add(n);
            }
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
                    if (attempts.incrementAndGet() > 1) {
                        throw new CommonException(DatabaseResultCodeEnum.UNAVAILABLE);
                    }
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

    @Test
    void scopeKeysAndEqualityPreserveDateAndMaximumIdentity() {
        SequenceScope first = scope("identity", 100);
        SequenceScope same = scope("identity", 100);
        assertThat(first).isEqualTo(same).hasSameHashCodeAs(same);
        assertThat(first.equals(null)).isFalse();
        assertThat(first.equals("identity")).isFalse();
        assertThat(first).isNotEqualTo(scope("other", 100)).isNotEqualTo(scope("identity", 101));
        SequenceScope persistent = new SequenceScope("identity", null, 100);
        assertThat(persistent.key()).isEqualTo("identity:persistent");
        assertThat(persistent).isNotEqualTo(first);
        assertThat(new HashSet<>(List.of(first, same, persistent))).hasSize(2);
        assertThat(first.toString()).contains("\"name\":\"identity\"", "\"maximum\":100");
        for (String name : new String[] {null, "", "space name", "a".repeat(121)}) {
            assertThatThrownBy(() -> new SequenceScope(name, null, 1)).isInstanceOf(CommonException.class);
        }
        for (long maximum : new long[] {0, -1, Long.MAX_VALUE}) {
            assertThatThrownBy(() -> new SequenceScope("valid", null, maximum)).isInstanceOf(CommonException.class);
        }
        assertThat(new SequenceScope("a".repeat(120), null, Long.MAX_VALUE - 1).maximum())
                .isEqualTo(Long.MAX_VALUE - 1);
    }

    @Test
    void allocationRangesRejectCorruptionAndSupportLargestSafeEndpoint() {
        for (long[] bounds : new long[][] {{0, 1}, {2, 1}, {1, Long.MAX_VALUE}}) {
            assertThatThrownBy(() -> new SequenceRange(bounds[0], bounds[1]))
                    .isInstanceOfSatisfying(
                            CommonException.class,
                            error -> assertThat(error.getResultCode()).isEqualTo(DatabaseResultCodeEnum.CORRUPT_STATE));
        }
        assertThat(new SequenceRange(Long.MAX_VALUE - 1, Long.MAX_VALUE - 1).toString())
                .contains("\"first\":" + (Long.MAX_VALUE - 1), "\"last\":" + (Long.MAX_VALUE - 1));
    }

    @Test
    void validatesEveryCapacityAndWaitBoundaryBeforeCreatingWorkers() {
        List<java.util.function.Consumer<SequenceProperties>> invalid = List.of(
                p -> p.setStep(0),
                p -> p.setStep(1_000_001),
                p -> p.setMaxScopes(0),
                p -> p.setPrefetchThreads(0),
                p -> p.setQueueCapacity(0),
                p -> p.setWaitTimeout(null),
                p -> p.setWaitTimeout(Duration.ofMillis(-1)),
                p -> p.setWaitTimeout(Duration.ZERO),
                p -> p.setWaitTimeout(Duration.ofMinutes(1).plusNanos(1)));
        for (var mutation : invalid) {
            SequenceProperties p = properties(10);
            mutation.accept(p);
            assertThatThrownBy(() -> new SegmentSequence(new Store(), p, clock))
                    .isInstanceOfSatisfying(
                            CommonException.class,
                            error -> assertThat(error.getResultCode()).isEqualTo(DatabaseResultCodeEnum.INVALID_INPUT));
        }
        SequenceProperties maximum = properties(1_000_000);
        maximum.setWaitTimeout(Duration.ofMinutes(1));
        assertThatCode(maximum::validate).doesNotThrowAnyException();
        SequenceProperties minimum = properties(1);
        minimum.setMaxScopes(1);
        minimum.setPrefetchThreads(1);
        minimum.setQueueCapacity(1);
        minimum.setWaitTimeout(Duration.ofNanos(1));
        assertThatCode(minimum::validate).doesNotThrowAnyException();
    }

    @Test
    void invalidCallsCannotAllocateAndClosedBatchesFailConsistently() {
        Store store = new Store();
        SegmentSequence sequence = new SegmentSequence(store, properties(10), clock);
        try {
            assertThatThrownBy(() -> sequence.next(null)).isInstanceOf(CommonException.class);
            assertThatThrownBy(() -> sequence.nextBatch(null, 1)).isInstanceOf(CommonException.class);
            assertThatThrownBy(() -> sequence.nextBatch(scope("invalid", 100), 10001))
                    .isInstanceOf(CommonException.class);
            assertThat(store.calls).hasValue(0);
        } finally {
            sequence.close();
        }
        assertThatThrownBy(() -> sequence.nextBatch(scope("closed", 100), 1))
                .isInstanceOfSatisfying(
                        CommonException.class,
                        error -> assertThat(error.getResultCode()).isEqualTo(DatabaseResultCodeEnum.CLOSED));
        assertThat(store.calls).hasValue(0);
    }

    @Test
    void concurrentExhaustionNeverReturnsDuplicateOrOutOfRangeValues() throws Exception {
        SequenceScope scope = scope("exhaust-concurrent", 37);
        Set<Long> issued = ConcurrentHashMap.newKeySet();
        AtomicInteger exhausted = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        try (SegmentSequence sequence = new SegmentSequence(new Store(), properties(10), clock);
                ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> tasks = new ArrayList<>();
            for (int i = 0; i < 64; i++) {
                tasks.add(workers.submit(() -> {
                    start.await();
                    try {
                        assertThat(issued.add(sequence.next(scope))).isTrue();
                    } catch (CommonException failure) {
                        assertThat(failure.getResultCode()).isEqualTo(DatabaseResultCodeEnum.EXHAUSTED);
                        exhausted.incrementAndGet();
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> task : tasks) {
                task.get(5, TimeUnit.SECONDS);
            }
            assertThat(issued)
                    .containsExactlyInAnyOrderElementsOf(java.util.stream.LongStream.rangeClosed(1, 37)
                            .boxed()
                            .toList());
            assertThat(exhausted).hasValue(27);
        }
    }

    @Test
    void corruptReservationNeverIssuesNumbersAndRecordsSpecificFailure() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        try (SegmentSequence sequence =
                new SegmentSequence((scope, step) -> new SequenceRange(1, 101), properties(10), clock, registry)) {
            assertThatThrownBy(() -> sequence.next(scope("corrupt", 100)))
                    .isInstanceOfSatisfying(
                            CommonException.class,
                            error -> assertThat(error.getResultCode()).isEqualTo(DatabaseResultCodeEnum.CORRUPT_STATE));
            assertThat(registry.get("loadup.database.sequence.issued").counter().count())
                    .isZero();
            assertThat(registry.get("loadup.database.sequence.failures")
                            .tag("reason", "CORRUPT_STATE")
                            .counter()
                            .count())
                    .isEqualTo(1);
            org.awaitility.Awaitility.await()
                    .atMost(Duration.ofSeconds(2))
                    .untilAsserted(() -> assertThat(registry.get("loadup.database.sequence.refill")
                                    .timer()
                                    .count())
                            .isEqualTo(1));
        } finally {
            registry.close();
        }
    }

    @Test
    void allocatorFailuresPreserveBusinessCodesAndWrapUnexpectedFailures() {
        RuntimeException[] failures = {
            new IllegalArgumentException("synthetic transport failure"),
            new CommonException(io.github.loadup.commons.enums.CommonResultCodeEnum.SYS_ERROR),
            new CommonException(DatabaseResultCodeEnum.EXHAUSTED)
        };
        for (RuntimeException failure : failures) {
            SimpleMeterRegistry registry = new SimpleMeterRegistry();
            try (SegmentSequence sequence = new SegmentSequence(
                    (scope, step) -> {
                        throw failure;
                    },
                    properties(10),
                    clock,
                    registry)) {
                assertThatThrownBy(() -> sequence.next(scope("failure", 100)))
                        .isInstanceOfSatisfying(CommonException.class, error -> {
                            if (failure instanceof CommonException) {
                                assertThat(error).isSameAs(failure);
                            } else {
                                assertThat(error.getResultCode()).isEqualTo(DatabaseResultCodeEnum.UNAVAILABLE);
                                assertThat(error.getCause()).isSameAs(failure);
                            }
                        });
                String reason = failure instanceof CommonException common
                                && common.getResultCode() == DatabaseResultCodeEnum.EXHAUSTED
                        ? "EXHAUSTED"
                        : "UNAVAILABLE";
                assertThat(registry.get("loadup.database.sequence.failures")
                                .tag("reason", reason)
                                .counter()
                                .count())
                        .isEqualTo(1);
                assertThat(registry.get("loadup.database.sequence.issued")
                                .counter()
                                .count())
                        .isZero();
            } finally {
                registry.close();
            }
        }
    }

    @Test
    void interruptingCallerPreservesItsInterruptAndLeavesAllocationReusable() throws Exception {
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        Store store = new Store();
        try (SegmentSequence sequence = new SegmentSequence(
                (scope, step) -> {
                    entered.countDown();
                    try {
                        release.await();
                    } catch (InterruptedException failure) {
                        Thread.currentThread().interrupt();
                        throw new CommonException(DatabaseResultCodeEnum.UNAVAILABLE, failure);
                    }
                    return store.reserve(scope, step);
                },
                properties(10),
                clock)) {
            AtomicReference<Throwable> unexpected = new AtomicReference<>();
            Thread caller = Thread.ofVirtual().start(() -> {
                try {
                    sequence.next(scope("interrupted", 100));
                    unexpected.set(new AssertionError("Expected interruption"));
                } catch (CommonException failure) {
                    try {
                        assertThat(failure.getResultCode()).isEqualTo(DatabaseResultCodeEnum.UNAVAILABLE);
                        assertThat(failure.getCause()).isInstanceOf(InterruptedException.class);
                        assertThat(Thread.currentThread().isInterrupted()).isTrue();
                    } catch (Throwable assertion) {
                        unexpected.set(assertion);
                    }
                }
            });
            try {
                assertThat(entered.await(1, TimeUnit.SECONDS)).isTrue();
                caller.interrupt();
                caller.join(2000);
                assertThat(caller.isAlive()).isFalse();
                assertThat(unexpected.get()).isNull();
                release.countDown();
                assertThat(sequence.next(scope("interrupted", 100))).isEqualTo(1);
            } finally {
                release.countDown();
                caller.interrupt();
            }
        }
    }

    @Test
    void persistentScopesAreNotEvictedWhenDatedScopesExpire() {
        SequenceProperties p = properties(10);
        p.setMaxScopes(2);
        SequenceScope persistent = new SequenceScope("persistent", null, 1000);
        try (SegmentSequence sequence = new SegmentSequence(new Store(), p, clock)) {
            assertThat(sequence.next(persistent)).isEqualTo(1);
            assertThat(sequence.next(new SequenceScope("old", LocalDate.of(2026, 10, 11), 1000)))
                    .isEqualTo(1);
            assertThat(sequence.next(scope("today", 1000))).isEqualTo(1);
            assertThat(sequence.next(persistent)).isEqualTo(2);
            assertThatThrownBy(() -> sequence.next(scope("another", 1000)))
                    .isInstanceOfSatisfying(
                            CommonException.class,
                            error -> assertThat(error.getResultCode()).isEqualTo(DatabaseResultCodeEnum.SCOPE_LIMIT));
        }
    }

    @Test
    void largestSequenceEndpointDoesNotOverflowAtomicCursor() {
        long maximum = Long.MAX_VALUE - 1;
        SequenceScope scope = new SequenceScope("long-endpoint", null, maximum);
        try (SegmentSequence sequence = new SegmentSequence(
                (requested, step) -> new SequenceRange(maximum - 1, maximum), properties(10), clock)) {
            assertThat(sequence.next(scope)).isEqualTo(maximum - 1);
            assertThat(sequence.next(scope)).isEqualTo(maximum);
            assertThatThrownBy(() -> sequence.next(scope))
                    .isInstanceOfSatisfying(
                            CommonException.class,
                            error -> assertThat(error.getResultCode()).isEqualTo(DatabaseResultCodeEnum.EXHAUSTED));
        }
    }

    private static final class Store implements SequenceRangeAllocator {
        private final Map<String, Long> maxima = new HashMap<>();
        private final AtomicInteger calls = new AtomicInteger();

        public synchronized SequenceRange reserve(SequenceScope scope, int step) {
            calls.incrementAndGet();
            long old = maxima.getOrDefault(scope.key(), 0L);
            if (old >= scope.maximum()) {
                throw new CommonException(DatabaseResultCodeEnum.EXHAUSTED);
            }
            long last = old + Math.min(step, scope.maximum() - old);
            maxima.put(scope.key(), last);
            return new SequenceRange(old + 1, last);
        }
    }
}
