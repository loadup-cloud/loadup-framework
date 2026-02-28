package io.github.loadup.modules.log.client.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AuditLogDTO {

    private String id;
    private String userId;
    private String username;
    private String dataType;
    private String dataId;
    private String action;
    private String beforeData;
    private String afterData;
    private String diffData;
    private String reason;
    private String ip;
    private LocalDateTime operationTime;
}

