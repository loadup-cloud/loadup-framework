package io.github.loadup.components.database.flex.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;

/**
 * Probe entity used only to verify generated mappers and logical deletion.
 */
@Table("flex_probe")
public class FlexProbeDO extends BaseDO {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }
}
