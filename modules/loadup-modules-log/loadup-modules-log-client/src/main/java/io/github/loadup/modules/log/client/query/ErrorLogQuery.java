package io.github.loadup.modules.log.client.query;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ErrorLogQuery {

    private String userId;
    private String errorType;
    private String errorCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer pageNum;
    private Integer pageSize;
}

