package io.github.loadup.modules.config.domain.model;

/*-
 * #%L
 * Loadup Modules Config Domain
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

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, configKey, oldValue, newValue, changeType, operator, remark, createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigHistory other = (ConfigHistory) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(configKey, other.configKey)) return false;
        if (!java.util.Objects.equals(oldValue, other.oldValue)) return false;
        if (!java.util.Objects.equals(newValue, other.newValue)) return false;
        if (!java.util.Objects.equals(changeType, other.changeType)) return false;
        if (!java.util.Objects.equals(operator, other.operator)) return false;
        if (!java.util.Objects.equals(remark, other.remark)) return false;
        if (!java.util.Objects.equals(createdAt, other.createdAt)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "ConfigHistory(" + "id=" + id + ", " + "configKey=" + configKey + ", " + "oldValue=" + oldValue + ", "
                + "newValue=" + newValue + ", " + "changeType=" + changeType + ", " + "operator=" + operator + ", "
                + "remark=" + remark + ", " + "createdAt=" + createdAt + ")";
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
}
