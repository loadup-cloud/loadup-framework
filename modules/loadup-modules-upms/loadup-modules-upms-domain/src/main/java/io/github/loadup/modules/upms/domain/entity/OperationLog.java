package io.github.loadup.modules.upms.domain.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Operation Log Entity - User operation audit log
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {

  private String id;

  private String traceId;

  private String userId;

  private String username;

  private String operationType;

  private String operationModule;

  private String operationDesc;
  private LocalDateTime operationTime;

  private String requestMethod;

  private String requestUrl;

  private String requestParams;

  private String responseResult;

  private String ipAddress;

  private String userAgent;

  private Long executionTime;

  /** Status: 1-Success, 0-Failure */
  private Short status;

  private String errorMessage;

  private LocalDateTime createdTime;
}
