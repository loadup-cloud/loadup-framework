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
                java.time.LocalDateTime.now(),
                null,
                null,
                java.time.LocalDateTime.now(),
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
