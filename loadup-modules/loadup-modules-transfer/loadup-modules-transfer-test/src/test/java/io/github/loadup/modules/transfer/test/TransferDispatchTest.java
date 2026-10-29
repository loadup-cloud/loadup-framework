/*
 * #%L
 * LoadUp Modules Transfer Test
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
package io.github.loadup.modules.transfer.test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.loadup.modules.file.client.facade.FileResourceFacade;
import io.github.loadup.modules.transfer.app.config.TransferTaskProperties;
import io.github.loadup.modules.transfer.app.converter.TransferDTOConverter;
import io.github.loadup.modules.transfer.app.service.TransferTaskService;
import io.github.loadup.modules.transfer.domain.gateway.TransferGateway;
import io.github.loadup.modules.transfer.domain.model.TransferKind;
import io.github.loadup.modules.transfer.domain.model.TransferStatus;
import io.github.loadup.modules.transfer.domain.model.TransferTask;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TransferDispatchTest {
    @Test
    void queuedRetryDoesNotReattachFilesOrDispatchAgain() {
        var gateway = mock(TransferGateway.class);
        var queued = task("queued");
        when(gateway.find("queued")).thenReturn(Optional.of(queued));
        var files = mock(FileResourceFacade.class);
        var converter = mock(TransferDTOConverter.class);
        var service = new TransferTaskService(
                gateway, files, List.of(), new TransferTaskProperties(), converter, Clock.systemDefaultZone());
        service.retry("tenant", "queued", "owner", false);
        verify(gateway).find("queued");
        verifyNoMoreInteractions(gateway);
        verifyNoInteractions(files);
        verify(converter).toDTO(queued);
    }

    @Test
    void deniedClaimsDoNotProcessAndFailuresDoNotAbortTheBatch() {
        var gateway = mock(TransferGateway.class);
        when(gateway.queued(20)).thenReturn(List.of("duplicate", "missing", "second"));
        when(gateway.find("duplicate")).thenReturn(Optional.of(task("duplicate")));
        when(gateway.find("missing")).thenReturn(Optional.empty());
        when(gateway.find("second")).thenReturn(Optional.of(task("second")));
        when(gateway.start(anyString(), anyString())).thenReturn(false);
        var service = new TransferTaskService(
                gateway,
                mock(FileResourceFacade.class),
                List.of(),
                new TransferTaskProperties(),
                mock(TransferDTOConverter.class),
                Clock.systemDefaultZone());
        assertThat(service.executeQueuedTasks()).isFalse();
        verify(gateway).start("tenant", "duplicate");
        verify(gateway).start("tenant", "second");
        verify(gateway, never()).options(anyString(), anyString());
    }

    @Test
    void emptyBatchSucceedsAndBatchSizeIsBounded() {
        var properties = new TransferTaskProperties();
        properties.setDispatchBatchSize(100);
        assertThat(properties.getDispatchBatchSize()).isEqualTo(100);
        assertThatThrownBy(() -> properties.setDispatchBatchSize(0))
                .isInstanceOf(io.github.loadup.commons.error.CommonException.class);
        assertThatThrownBy(() -> properties.setDispatchBatchSize(101))
                .isInstanceOf(io.github.loadup.commons.error.CommonException.class);
        var gateway = mock(TransferGateway.class);
        when(gateway.queued(100)).thenReturn(List.of());
        var service = new TransferTaskService(
                gateway,
                mock(FileResourceFacade.class),
                List.of(),
                properties,
                mock(TransferDTOConverter.class),
                Clock.systemDefaultZone());
        assertThat(service.executeQueuedTasks()).isTrue();
    }

    private static TransferTask task(String id) {
        return new TransferTask(
                id,
                "tenant",
                "owner",
                TransferKind.EXPORT,
                "test",
                null,
                null,
                TransferStatus.QUEUED,
                0,
                0,
                null,
                LocalDateTime.now(),
                null,
                null);
    }
}
