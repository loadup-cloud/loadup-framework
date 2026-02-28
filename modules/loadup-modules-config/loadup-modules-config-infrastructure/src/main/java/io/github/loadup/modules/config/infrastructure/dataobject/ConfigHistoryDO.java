package io.github.loadup.modules.config.infrastructure.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import lombok.*;

/**
 * config_history 表映射对象。
 *
 * <p>历史记录只追加不修改，不继承 BaseDO（无 updatedAt/tenantId/deleted 字段需求）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table("config_history")
public class ConfigHistoryDO extends BaseDO {

    private String configKey;
    private String oldValue;
    private String newValue;
    /**
     * CREATE / UPDATE / DELETE
     */
    private String changeType;
    private String operator;
    private String remark;
}

