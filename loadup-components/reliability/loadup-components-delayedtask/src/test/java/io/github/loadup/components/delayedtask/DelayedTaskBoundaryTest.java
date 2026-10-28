/*
 * #%L
 * LoadUp Components DelayedTask
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
package io.github.loadup.components.delayedtask;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.delayedtask.dataobject.DelayedTaskDO;
import io.github.loadup.components.delayedtask.mapper.DelayedTaskDOMapper;
import io.micrometer.observation.ObservationRegistry;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class DelayedTaskBoundaryTest {
    @Test
    void lostClaimContinuesToTheNextCandidate() {
        var mapper = mock(DelayedTaskDOMapper.class);
        var store = new DelayedTaskStore(mapper);
        var properties = new DelayedTaskProperties();
        var now = LocalDateTime.now();
        var first = new DelayedTaskDO();
        var second = new DelayedTaskDO();
        first.setId("first");
        second.setId("second");
        for (var row : List.of(first, second)) {
            row.setStatus(DelayedTaskStatus.PENDING);
            row.setAttempts(0);
            row.setExecuteAt(now);
            row.setNextExecuteAt(now);
        }
        when(mapper.selectListByQuery(any())).thenReturn(List.of(first, second));
        when(mapper.updateByQuery(any(DelayedTaskDO.class), any(com.mybatisflex.core.query.QueryWrapper.class)))
                .thenReturn(0, 1);
        assertThat(store.claim(now, properties)).contains(second);
        assertThat(first.getStatus()).isEqualTo(DelayedTaskStatus.PENDING);
        assertThat(first.getAttempts()).isZero();
        assertThat(second.getStatus()).isEqualTo(DelayedTaskStatus.RUNNING);
        assertThat(second.getAttempts()).isEqualTo(1);
        assertThat(second.getClaimToken()).isNotBlank();
        assertThat(second.getLeaseUntil()).isEqualTo(now.plus(properties.getLease()));
    }

    @Test
    void nullAndEmptyCandidatesDoNotAttemptClaims() {
        var mapper = mock(DelayedTaskDOMapper.class);
        var store = new DelayedTaskStore(mapper);
        when(mapper.selectListByQuery(any())).thenReturn(null, List.of());
        var properties = new DelayedTaskProperties();
        assertThat(store.claim(LocalDateTime.now(), properties)).isEmpty();
        assertThat(store.claim(LocalDateTime.now(), properties)).isEmpty();
        verify(mapper, times(2)).selectListByQuery(any());
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void rejectsInvalidBusinessKeysAndDeadlines() {
        var template = new DelayedTaskTemplate(
                mock(DelayedTaskStore.class),
                mock(DelayedTaskConverter.class),
                new DelayedTaskProperties(),
                new DatabaseProperties(),
                Clock.systemDefaultZone(), mock(org.springframework.transaction.PlatformTransactionManager.class));
        for (String type : Arrays.asList(null, "", "bad name"))
            assertThatThrownBy(() -> template.find(type, "id")).isInstanceOf(CommonException.class);
        for (String id : Arrays.asList(null, "", " ", "x".repeat(129)))
            assertThatThrownBy(() -> template.find("order.close", id)).isInstanceOf(CommonException.class);
        assertThatThrownBy(() -> template.register("order.close", "id", null)).isInstanceOf(CommonException.class);
        assertThatThrownBy(() -> template.reschedule("order.close", "id", null)).isInstanceOf(CommonException.class);
    }

    @Test
    void optionalTenantFallsBackToConfiguredTenant() {
        var store = mock(DelayedTaskStore.class);
        when(store.mapper()).thenReturn(mock(DelayedTaskDOMapper.class));
        var database = new DatabaseProperties();
        var template = new DelayedTaskTemplate(
                store,
                mock(DelayedTaskConverter.class),
                new DelayedTaskProperties(),
                database,
                Clock.systemDefaultZone(), mock(org.springframework.transaction.PlatformTransactionManager.class));
        database.getMultiTenant().setEnabled(false);
        assertThat(template.find("order.close", "id")).isEmpty();
        database.getMultiTenant().setEnabled(true);
        database.getMultiTenant().setRequired(false);
        assertThat(template.find("order.close", "id")).isEmpty();
        io.github.loadup.commons.context.ContextHolder.runWith(
                io.github.loadup.commons.context.ExecutionContext.empty()
                        .with(io.github.loadup.commons.context.ContextKeys.TENANT_ID, " "),
                () -> assertThat(template.find("order.close", "id")).isEmpty());
    }

    @Test
    void validatesHandlersAndPreservesInterruptionAndLostLease() throws Exception {
        var store = mock(DelayedTaskStore.class);
        var converter = mock(DelayedTaskConverter.class);
        var props = new DelayedTaskProperties();
        props.setBatchSize(1);
        var handler = mock(DelayedTaskHandler.class);
        when(handler.taskType()).thenReturn("order.close");
        assertThatThrownBy(() -> new DelayedTaskDispatcher(
                        store,
                        converter,
                        props,
                        Clock.systemDefaultZone(),
                        ObservationRegistry.NOOP,
                        List.of(handler, handler)))
                .isInstanceOf(CommonException.class);
        when(handler.taskType()).thenReturn(null);
        assertThatThrownBy(() -> new DelayedTaskDispatcher(
                        store, converter, props, Clock.systemDefaultZone(), ObservationRegistry.NOOP, List.of(handler)))
                .isInstanceOf(CommonException.class);
        when(handler.taskType()).thenReturn("order.close");
        var dispatcher = new DelayedTaskDispatcher(
                store, converter, props, Clock.systemDefaultZone(), ObservationRegistry.NOOP, List.of(handler));
        assertThat(dispatcher.name()).isEqualTo("delayedtask.scan");
        var row = new DelayedTaskDO();
        when(store.claim(any(), same(props))).thenReturn(Optional.of(row));
        var task = new DelayedTask(
                "id",
                "tenant",
                "order.close",
                "order",
                LocalDateTime.now(),
                LocalDateTime.now(),
                DelayedTaskStatus.RUNNING,
                1,
                3,
                null,
                null);
        when(converter.toTask(row)).thenReturn(task);
        dispatcher.execute();
        doThrow(new InterruptedException()).when(handler).handle(task);
        try {
            dispatcher.execute();
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
            dispatcher.execute();
            verify(handler, times(2)).handle(task);
        } finally {
            Thread.interrupted();
        }
    }
}
