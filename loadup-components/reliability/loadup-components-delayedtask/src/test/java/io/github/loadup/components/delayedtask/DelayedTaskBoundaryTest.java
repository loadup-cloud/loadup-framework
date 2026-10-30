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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.delayedtask.dataobject.DelayedTaskDO;
import io.micrometer.observation.ObservationRegistry;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DelayedTaskBoundaryTest {
    @Test
    void rejectsInvalidBusinessKeysAndDeadlines() {
        var template = new DelayedTaskTemplate(
                mock(DelayedTaskStore.class),
                mock(DelayedTaskConverter.class),
                new DelayedTaskProperties(),
                Clock.systemDefaultZone(),
                mock(javax.sql.DataSource.class));
        for (String type : Arrays.asList(null, "", "bad name")) {
            assertThatThrownBy(() -> template.find(type, "id")).isInstanceOf(CommonException.class);
        }
        for (String id : Arrays.asList(null, "", " ", "x".repeat(129))) {
            assertThatThrownBy(() -> template.find("order.close", id)).isInstanceOf(CommonException.class);
        }
        assertThatThrownBy(() -> template.register("order.close", "id", null)).isInstanceOf(CommonException.class);
        assertThatThrownBy(() -> template.reschedule("order.close", "id", null)).isInstanceOf(CommonException.class);
    }

    @Test
    void findDoesNotRequireTenantContext() {
        var store = mock(DelayedTaskStore.class);
        when(store.find(any(), any(), eq(false))).thenReturn(Optional.empty());
        var template = new DelayedTaskTemplate(
                store,
                mock(DelayedTaskConverter.class),
                new DelayedTaskProperties(),
                Clock.systemDefaultZone(),
                mock(javax.sql.DataSource.class));
        assertThat(template.find("order.close", "id")).isEmpty();
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
