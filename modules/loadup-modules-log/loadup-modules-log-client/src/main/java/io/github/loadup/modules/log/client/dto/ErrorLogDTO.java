package io.github.loadup.modules.log.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error log DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLogDTO {

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
    private java.time.LocalDateTime errorTime;
}

