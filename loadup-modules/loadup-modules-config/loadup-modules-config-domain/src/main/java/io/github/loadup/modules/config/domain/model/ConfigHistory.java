package io.github.loadup.modules.config.domain.model;

/*-
 * #%L
 * Loadup Modules Config Domain
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

import io.github.loadup.modules.config.domain.enums.ChangeType;
import java.time.LocalDateTime;

/**
 * Domain model for configuration change history.
 *
 * <p>Pure POJO — no persistence framework annotations.
 */
public class ConfigHistory {

    private String id;
    private String configKey;
    private String oldValue;
    private String newValue;
    private ChangeType changeType;
    private String operator;
    private String remark;
    private LocalDateTime createdAt;

    public ConfigHistory(
            String id,
            String configKey,
            String oldValue,
            String newValue,
            ChangeType changeType,
            String operator,
            String remark,
            LocalDateTime createdAt) {
        this.id = id;
        this.configKey = configKey;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeType = changeType;
        this.operator = operator;
        this.remark = remark;
        this.createdAt = createdAt;
    }

    public ConfigHistory() {}

    public String getId() {
        return this.id;
    }

    public String getConfigKey() {
        return this.configKey;
    }

    public String getOldValue() {
        return this.oldValue;
    }

    public String getNewValue() {
        return this.newValue;
    }

    public ChangeType getChangeType() {
        return this.changeType;
    }

    public String getOperator() {
        return this.operator;
    }

    public String getRemark() {
        return this.remark;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String configKey;
        private String oldValue;
        private String newValue;
        private ChangeType changeType;
        private String operator;
        private String remark;
        private LocalDateTime createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder configKey(String configKey) {
            this.configKey = configKey;
            return this;
        }

        public Builder oldValue(String oldValue) {
            this.oldValue = oldValue;
            return this;
        }

        public Builder newValue(String newValue) {
            this.newValue = newValue;
            return this;
        }

        public Builder changeType(ChangeType changeType) {
            this.changeType = changeType;
            return this;
        }

        public Builder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ConfigHistory build() {
            return new ConfigHistory(
                    this.id,
                    this.configKey,
                    this.oldValue,
                    this.newValue,
                    this.changeType,
                    this.operator,
                    this.remark,
                    this.createdAt);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
