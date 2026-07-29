package io.github.loadup.modules.config.infrastructure.dataobject;

/*-
 * #%L
 * Loadup Modules Config Infrastructure
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;

/**
 * config_history 表映射对象。
 *
 * <p>历史记录只追加不修改，不继承 BaseDO（无 updatedAt/tenantId/deleted 字段需求）。
 */
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

    public ConfigHistoryDO(
            String configKey, String oldValue, String newValue, String changeType, String operator, String remark) {
        this.configKey = configKey;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeType = changeType;
        this.operator = operator;
        this.remark = remark;
    }

    public ConfigHistoryDO() {}

    public String getConfigKey() {
        return this.configKey;
    }

    public String getOldValue() {
        return this.oldValue;
    }

    public String getNewValue() {
        return this.newValue;
    }

    public String getChangeType() {
        return this.changeType;
    }

    public String getOperator() {
        return this.operator;
    }

    public String getRemark() {
        return this.remark;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
