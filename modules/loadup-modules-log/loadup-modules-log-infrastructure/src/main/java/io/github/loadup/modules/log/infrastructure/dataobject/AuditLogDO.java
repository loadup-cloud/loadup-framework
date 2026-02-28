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
@Table("audit_log")
public class AuditLogDO extends BaseDO {

    private String userId;
    private String username;
    private String dataType;
    private String dataId;
    private String action;
    private String beforeData;
    private String afterData;
    private String diffData;
    private String reason;
    private String ip;
    private java.time.LocalDateTime operationTime;
}

