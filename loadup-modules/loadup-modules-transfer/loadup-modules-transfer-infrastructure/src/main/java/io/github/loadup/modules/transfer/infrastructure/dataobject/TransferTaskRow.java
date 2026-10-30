package io.github.loadup.modules.transfer.infrastructure.dataobject;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** Row model for transfer tasks, read through Spring Data JDBC. */
@Table("transfer_task")
public record TransferTaskRow(
        @Id @Column("id") String id,
        @Column("tenant_id") String tenantId,
        @Column("owner_id") String ownerId,
        @Column("kind") String kind,
        @Column("handler_key") String handlerKey,
        @Column("source_file_id") String sourceFileId,
        @Column("result_file_id") String resultFileId,
        @Column("status") String status,
        @Column("processed_count") long processedCount,
        @Column("total_count") long totalCount,
        @Column("error_message") String errorMessage,
        @Column("created_at") LocalDateTime createdAt,
        @Column("started_at") LocalDateTime startedAt,
        @Column("finished_at") LocalDateTime finishedAt,
        @Column("updated_at") LocalDateTime updatedAt,
        @Column("deleted") boolean deleted) {}
