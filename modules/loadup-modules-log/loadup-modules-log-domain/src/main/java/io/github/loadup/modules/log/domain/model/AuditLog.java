package io.github.loadup.modules.log.domain.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model for audit log.
 *
 * <p>Pure POJO — no persistence framework annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    private String id;
    private String userId;
    private String username;
    /** USER / ROLE / PERMISSION / CONFIG / ... */
    private String dataType;
    private String dataId;
    /** CREATE / UPDATE / DELETE / ASSIGN / ... */
    private String action;
    private String beforeData;
    private String afterData;
    private String diffData;
    private String reason;
    private String ip;
    private LocalDateTime operationTime;
    private LocalDateTime createdAt;
}

