package com.github.loadup.upms.api.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Operation Log DTO
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogDTO {

  private String id;
  private String userId;
  private String username;
  private String operationType;
  private String operationName;
  private String method;
  private String requestUri;
  private String requestParams;
  private String ipAddress;
  private String location;
  private String browser;
  private String os;
  private Integer status;
  private String errorMessage;
  private Long executionTime;
  private LocalDateTime operationTime;
}
