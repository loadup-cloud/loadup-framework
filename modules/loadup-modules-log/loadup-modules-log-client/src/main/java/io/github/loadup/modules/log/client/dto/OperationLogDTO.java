package io.github.loadup.modules.log.client.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OperationLogDTO {

    private String id;
    private String userId;
    private String username;
    private String module;
    private String operationType;
    private String description;
    private String method;
    private String requestParams;
    private String responseResult;
    private Long duration;
    private Boolean success;
    private String errorMessage;
    private String ip;
    private String userAgent;
    private LocalDateTime operationTime;
}

