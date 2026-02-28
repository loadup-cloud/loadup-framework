package io.github.loadup.modules.log.client.query;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AuditLogQuery {

    private String userId;
    private String dataType;
    private String dataId;
    private String action;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer pageNum;
    private Integer pageSize;
}

