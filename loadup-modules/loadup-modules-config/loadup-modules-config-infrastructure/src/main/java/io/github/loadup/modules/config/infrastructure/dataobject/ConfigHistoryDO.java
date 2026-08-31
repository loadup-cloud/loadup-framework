package io.github.loadup.modules.config.infrastructure.dataobject;

/*-
 * #%L
 * Loadup Modules Config Infrastructure
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
