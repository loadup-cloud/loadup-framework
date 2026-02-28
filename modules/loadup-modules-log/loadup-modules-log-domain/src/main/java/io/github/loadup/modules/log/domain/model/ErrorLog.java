package io.github.loadup.modules.log.domain.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model for error log. Pure POJO, no framework annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLog {

    private String id;
    private String userId;
    /** BUSINESS / SYSTEM / THIRD_PARTY */
    private String errorType;
    private String errorCode;
    private String errorMessage;
    private String stackTrace;
    private String requestUrl;
    private String requestMethod;
    private String requestParams;
    private String ip;
    private LocalDateTime errorTime;
    private LocalDateTime createdAt;
}

