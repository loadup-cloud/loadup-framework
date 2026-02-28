package io.github.loadup.modules.log.domain.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model for operation log.
 *
 * <p>Pure POJO — no persistence framework annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {

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
    private LocalDateTime createdAt;
}

