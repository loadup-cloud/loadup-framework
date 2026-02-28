package io.github.loadup.modules.log.client.query;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LogStatisticsQuery {

    private String userId;
    private String module;
    private String operationType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

