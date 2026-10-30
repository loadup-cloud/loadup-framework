package io.github.loadup.modules.transfer.infrastructure.dataobject;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** Row model for transfer task options, read through Spring Data JDBC. */
@Table("transfer_task_option")
public record TransferTaskOptionRow(
        @Id @Column("id") String id,
        @Column("tenant_id") String tenantId,
        @Column("task_id") String taskId,
        @Column("option_name") String optionName,
        @Column("option_value") String optionValue,
        @Column("created_at") LocalDateTime createdAt,
        @Column("updated_at") LocalDateTime updatedAt,
        @Column("deleted") boolean deleted) {}
