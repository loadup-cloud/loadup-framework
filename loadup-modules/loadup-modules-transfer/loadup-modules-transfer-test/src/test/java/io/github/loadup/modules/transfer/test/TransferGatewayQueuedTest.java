package io.github.loadup.modules.transfer.test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.transfer.infrastructure.dataobject.TransferTaskRow;
import io.github.loadup.modules.transfer.infrastructure.repository.TransferGatewayImpl;
import io.github.loadup.modules.transfer.infrastructure.repository.TransferTaskOptionRepository;
import io.github.loadup.modules.transfer.infrastructure.repository.TransferTaskRepository;
import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

class TransferGatewayQueuedTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final TransferTaskRepository tasks = mock(TransferTaskRepository.class);
    private final TransferTaskOptionRepository options = mock(TransferTaskOptionRepository.class);
    private final TransferGatewayImpl gateway =
            new TransferGatewayImpl(jdbc, tasks, options, () -> "id", Clock.systemDefaultZone());

    @Test
    void preservesCandidateOrderAndRejectsInvalidBatchSizes() {
        Page<TransferTaskRow> page = new PageImpl<>(List.of(row("first"), row("second")));
        when(tasks.findByStatusAndDeletedFalse(eq("QUEUED"), any(Pageable.class)))
                .thenReturn(page);
        assertThat(gateway.queued(2)).containsExactly("first", "second");
        clearInvocations(tasks);
        for (int limit : new int[] {0, -1, 101}) {
            assertThatThrownBy(() -> gateway.queued(limit)).isInstanceOf(CommonException.class);
        }
        verifyNoInteractions(tasks);
        verifyNoInteractions(jdbc);
    }

    private static TransferTaskRow row(String id) {
        return new TransferTaskRow(
                id,
                "tenant",
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
}
