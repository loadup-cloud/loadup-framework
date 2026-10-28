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
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(classes = DatabaseSequenceIT.Application.class)
@ActiveProfiles("test")
@EnableTestContainers(ContainerType.POSTGRESQL)
class DatabaseSequenceIT {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class Application {}

    private final SequenceRangeAllocator allocator;
    private final PlatformTransactionManager manager;
    private final io.github.loadup.components.database.id.IdGenerator idGenerator;
    private final io.github.loadup.components.database.sequence.mapper.SequenceAllocationDOMapper mapper;

    @Autowired
    DatabaseSequenceIT(
            SequenceRangeAllocator allocator,
            PlatformTransactionManager manager,
            io.github.loadup.components.database.id.IdGenerator idGenerator,
            io.github.loadup.components.database.sequence.mapper.SequenceAllocationDOMapper mapper) {
        this.allocator = allocator;
        this.manager = manager;
        this.idGenerator = idGenerator;
        this.mapper = mapper;
    }

    private SequenceScope scope(long maximum) {
        return new SequenceScope("it-" + UUID.randomUUID(), LocalDate.now(), maximum);
    }

    @Test
    void concurrentInitializationAndMultipleClients() throws Exception {
        SequenceScope scope = scope(10000);
        Set<Long> seen = ConcurrentHashMap.newKeySet();
        try (ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> tasks = new ArrayList<>();
            for (int i = 0; i < 16; i++)
                tasks.add(workers.submit(() -> {
                    SequenceRange r = allocator.reserve(scope, 50);
                    for (long n = r.first(); n <= r.last(); n++)
                        assertThat(seen.add(n)).isTrue();
                }));
            for (Future<?> f : tasks) f.get(20, TimeUnit.SECONDS);
            assertThat(seen).hasSize(800);
        }
    }

    @Test
    void independentCommitSurvivesCallerRollbackAndTenantChanges() {
        SequenceScope scope = scope(100);
        TransactionTemplate caller = new TransactionTemplate(manager);
        caller.executeWithoutResult(status -> {
            assertThat(TenantUtil.callWithTenant("merchant-a", () -> allocator.reserve(scope, 10))
                            .first())
                    .isEqualTo(1);
            status.setRollbackOnly();
        });
        assertThat(TenantUtil.callWithTenant("merchant-b", () -> allocator.reserve(scope, 10))
                        .first())
                .isEqualTo(11);
    }

    @Test
    void finalRangeAndMaximumConflict() {
        SequenceScope scope = scope(13);
        assertThat(allocator.reserve(scope, 10)).isEqualTo(new SequenceRange(1, 10));
        assertThat(allocator.reserve(scope, 10)).isEqualTo(new SequenceRange(11, 13));
        assertThatThrownBy(() -> allocator.reserve(scope, 10))
                .isInstanceOfSatisfying(
                        CommonException.class,
                        e -> assertThat(e.getResultCode()).isEqualTo(DatabaseResultCodeEnum.EXHAUSTED));
        assertThatThrownBy(() -> allocator.reserve(new SequenceScope(scope.name(), scope.date(), 14), 10))
                .isInstanceOfSatisfying(
                        CommonException.class,
                        e -> assertThat(e.getResultCode()).isEqualTo(DatabaseResultCodeEnum.CONFIG_CONFLICT));
    }

    @Test
    void cachedClientsAndConcurrentBatchesUseDisjointDatabaseRanges() throws Exception {
        SequenceScope scope = scope(100000);
        Set<Long> seen = ConcurrentHashMap.newKeySet();
        SequenceProperties properties = new SequenceProperties();
        properties.setStep(500);
        try (SegmentSequence a = new SegmentSequence(allocator, properties, Clock.systemDefaultZone());
                SegmentSequence b = new SegmentSequence(allocator, properties, Clock.systemDefaultZone());
                ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> tasks = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                Sequence client = i % 2 == 0 ? a : b;
                tasks.add(workers.submit(() -> {
                    for (int round = 0; round < 20; round++)
                        for (long id : client.nextBatch(scope, 37))
                            assertThat(seen.add(id)).isTrue();
                }));
            }
            for (Future<?> task : tasks) task.get(20, TimeUnit.SECONDS);
            assertThat(seen).hasSize(5920);
        }
    }

    @Test
    void flexUsesDefaultBusinessIdsAndPreservesExplicitIdsWithoutRecursion() {
        assertThat(idGenerator).isInstanceOf(io.github.loadup.components.database.id.BusinessIdGenerator.class);
        var formatter = new io.github.loadup.components.database.id.BusinessIdFormatter();
        var row = new io.github.loadup.components.database.sequence.dataobject.SequenceAllocationDO();
        row.setScopeKey("auto-test-" + UUID.randomUUID());
        row.setMaxAllocated(0L);
        row.setMaxValue(100L);
        TenantUtil.runWithTenant("merchant-auto", () -> mapper.insert(row));
        assertThat(row.getId()).hasSize(32);
        assertThat(formatter.parse(row.getId()).command().eventCode()).isEqualTo("00000000");
        assertThat(TenantUtil.callWithTenant("merchant-auto", () -> mapper.selectOneById(row.getId()))
                        .getId())
                .isEqualTo(row.getId());
        var explicit = new io.github.loadup.components.database.sequence.dataobject.SequenceAllocationDO();
        explicit.setId("supplied-id-" + UUID.randomUUID());
        explicit.setScopeKey("explicit-test-" + UUID.randomUUID());
        explicit.setMaxAllocated(0L);
        explicit.setMaxValue(100L);
        String expected = explicit.getId();
        TenantUtil.runWithTenant("merchant-auto", () -> mapper.insert(explicit));
        assertThat(explicit.getId()).isEqualTo(expected);
    }
}
