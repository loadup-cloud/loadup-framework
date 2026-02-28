package io.github.loadup.modules.config.domain.model;

import io.github.loadup.modules.config.domain.enums.ChangeType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model for configuration change history.
 *
 * <p>Pure POJO — no persistence framework annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigHistory {

    private String id;
    private String configKey;
    private String oldValue;
    private String newValue;
    private ChangeType changeType;
    private String operator;
    private String remark;
    private LocalDateTime createdAt;
}

