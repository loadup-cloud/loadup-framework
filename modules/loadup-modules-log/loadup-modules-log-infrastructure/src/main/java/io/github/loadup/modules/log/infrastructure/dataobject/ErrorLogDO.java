package io.github.loadup.modules.log.infrastructure.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table("error_log")
public class ErrorLogDO extends BaseDO {

    private String userId;
    private String errorType;
    private String errorCode;
    private String errorMessage;
    private String stackTrace;
    private String requestUrl;
    private String requestMethod;
    private String requestParams;
    private String ip;
    private LocalDateTime errorTime;
}

