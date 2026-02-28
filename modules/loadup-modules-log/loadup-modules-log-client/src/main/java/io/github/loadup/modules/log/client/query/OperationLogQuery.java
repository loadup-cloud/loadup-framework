package io.github.loadup.modules.log.client.query;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OperationLogQuery {

    private String userId;
    private String module;
    private String operationType;
    private Boolean success;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer pageNum;
    private Integer pageSize;
}

