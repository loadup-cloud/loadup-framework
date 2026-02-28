package io.github.loadup.modules.log.infrastructure.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table("operation_log")
public class OperationLogDO extends BaseDO {

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
    private java.time.LocalDateTime operationTime;
}

