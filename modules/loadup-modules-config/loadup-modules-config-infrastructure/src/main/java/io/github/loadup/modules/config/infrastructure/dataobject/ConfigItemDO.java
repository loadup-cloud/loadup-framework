package io.github.loadup.modules.config.infrastructure.dataobject;

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
@Table("config_item")
public class ConfigItemDO extends BaseDO {

    private String configKey;
    private String configValue;
    /** STRING / INTEGER / LONG / DOUBLE / BOOLEAN / JSON */
    private String valueType;
    private String category;
    private String description;
    private Boolean editable;
    private Boolean encrypted;
    private Boolean systemDefined;
    private Integer sortOrder;
    private Boolean enabled;
    private String createdBy;
    private String updatedBy;
}
