package io.github.loadup.modules.upms.infrastructure.dataobject;

import io.github.loadup.commons.dataobject.BaseDO;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OperationLog Data Object
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("upms_operation_log")
public class OperationLogDO extends BaseDO {

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

  private Short status;

  private String errorMessage;
}
