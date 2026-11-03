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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.transfer.infrastructure.dataobject.TransferTaskRow;
import io.github.loadup.modules.transfer.infrastructure.mapper.TransferTaskOptionRowMapper;
import io.github.loadup.modules.transfer.infrastructure.mapper.TransferTaskRowMapper;
import io.github.loadup.modules.transfer.infrastructure.repository.TransferGatewayImpl;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class TransferGatewayQueuedTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final TransferTaskRowMapper tasks = mock(TransferTaskRowMapper.class);
    private final TransferTaskOptionRowMapper options = mock(TransferTaskOptionRowMapper.class);
    private final TransferGatewayImpl gateway =
            new TransferGatewayImpl(jdbc, tasks, options, () -> "id", Clock.systemDefaultZone());

    private static TransferTaskRow row(String id) {
        return new TransferTaskRow(
                id,
                "owner",
                "IMPORT",
                "handler",
                null,
                null,
                "QUEUED",
                0,
                0,
                null,
                LocalDateTime.now(),
                null,
                null,
                LocalDateTime.now(),
                false);
    }

    @Test
    void preservesCandidateOrderAndRejectsInvalidBatchSizes() {
        when(tasks.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(row("first"), row("second")));
        assertThat(gateway.queued(2)).containsExactly("first", "second");
        clearInvocations(tasks);
        for (int limit : new int[] {0, -1, 101}) {
            assertThatThrownBy(() -> gateway.queued(limit)).isInstanceOf(CommonException.class);
        }
        verifyNoInteractions(tasks);
        verifyNoInteractions(options);
        verifyNoInteractions(jdbc);
    }
}
