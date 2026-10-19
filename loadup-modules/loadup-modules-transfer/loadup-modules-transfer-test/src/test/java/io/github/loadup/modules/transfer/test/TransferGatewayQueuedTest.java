/*
 * #%L
 * LoadUp Transfer Infrastructure
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.transfer.infrastructure.converter.TransferStorageConverter;
import io.github.loadup.modules.transfer.infrastructure.dataobject.TransferTaskDO;
import io.github.loadup.modules.transfer.infrastructure.mapper.TransferTaskDOMapper;
import io.github.loadup.modules.transfer.infrastructure.mapper.TransferTaskOptionDOMapper;
import io.github.loadup.modules.transfer.infrastructure.repository.TransferGatewayImpl;
import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.Test;

class TransferGatewayQueuedTest {
    private final TransferTaskDOMapper mapper = mock(TransferTaskDOMapper.class);
    private final TransferGatewayImpl gateway = new TransferGatewayImpl(
            mapper,
            mock(TransferTaskOptionDOMapper.class),
            mock(TransferStorageConverter.class),
            Clock.systemDefaultZone());

    @Test
    void nullOrEmptyQueryResultsAreEmptyQueues() {
        when(mapper.selectListByQuery(any())).thenReturn(null, List.of());
        assertThat(gateway.queued(20)).isEmpty();
        assertThat(gateway.queued(20)).isEmpty();
    }

    @Test
    void preservesCandidateOrderAndRejectsInvalidBatchSizes() {
        var first = new TransferTaskDO();
        first.setId("first");
        var second = new TransferTaskDO();
        second.setId("second");
        when(mapper.selectListByQuery(any())).thenReturn(List.of(first, second));
        assertThat(gateway.queued(2)).containsExactly("first", "second");
        clearInvocations(mapper);
        for (int limit : new int[] {0, -1, 101}) {
            assertThatThrownBy(() -> gateway.queued(limit)).isInstanceOf(CommonException.class);
        }
        verifyNoInteractions(mapper);
    }
}
