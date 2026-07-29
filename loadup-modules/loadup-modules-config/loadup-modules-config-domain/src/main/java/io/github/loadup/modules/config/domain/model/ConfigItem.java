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

import java.time.LocalDateTime;

/**
 * Domain model for a system configuration item.
 *
 * <p>Pure POJO — no persistence framework annotations.
 */
public class ConfigItem {

    private String id;
    private String configKey;
    private String configValue;
    /**
     * STRING / INTEGER / LONG / DOUBLE / BOOLEAN / JSON
     */
    private String valueType;

    private String category;
    private String description;
    private Boolean editable;
    private Boolean encrypted;
    private Boolean systemDefined;
    private Integer sortOrder;
    private Boolean enabled;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public ConfigItem(
            String id,
            String configKey,
            String configValue,
            String valueType,
            String category,
            String description,
            Boolean editable,
            Boolean encrypted,
            Boolean systemDefined,
            Integer sortOrder,
            Boolean enabled,
            String createdBy,
            LocalDateTime createdAt,
            String updatedBy,
            LocalDateTime updatedAt) {
        this.id = id;
        this.configKey = configKey;
        this.configValue = configValue;
        this.valueType = valueType;
        this.category = category;
        this.description = description;
        this.editable = editable;
        this.encrypted = encrypted;
        this.systemDefined = systemDefined;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public ConfigItem() {}

    public String getId() {
        return this.id;
    }

    public String getConfigKey() {
        return this.configKey;
    }

    public String getConfigValue() {
        return this.configValue;
    }

    public String getValueType() {
        return this.valueType;
    }

    public String getCategory() {
        return this.category;
    }

    public String getDescription() {
        return this.description;
    }

    public Boolean isEditable() {
        return this.editable;
    }

    public Boolean isEncrypted() {
        return this.encrypted;
    }

    public Boolean isSystemDefined() {
        return this.systemDefined;
    }

    public Integer getSortOrder() {
        return this.sortOrder;
    }

    public Boolean isEnabled() {
        return this.enabled;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public String getUpdatedBy() {
        return this.updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
    }

    public void setSystemDefined(Boolean systemDefined) {
        this.systemDefined = systemDefined;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String configKey;
        private String configValue;
        private String valueType;
        private String category;
        private String description;
        private Boolean editable;
        private Boolean encrypted;
        private Boolean systemDefined;
        private Integer sortOrder;
        private Boolean enabled;
        private String createdBy;
        private LocalDateTime createdAt;
        private String updatedBy;
        private LocalDateTime updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder configKey(String configKey) {
            this.configKey = configKey;
            return this;
        }

        public Builder configValue(String configValue) {
            this.configValue = configValue;
            return this;
        }

        public Builder valueType(String valueType) {
            this.valueType = valueType;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder editable(Boolean editable) {
            this.editable = editable;
            return this;
        }

        public Builder encrypted(Boolean encrypted) {
            this.encrypted = encrypted;
            return this;
        }

        public Builder systemDefined(Boolean systemDefined) {
            this.systemDefined = systemDefined;
            return this;
        }

        public Builder sortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ConfigItem build() {
            return new ConfigItem(
                    this.id,
                    this.configKey,
                    this.configValue,
                    this.valueType,
                    this.category,
                    this.description,
                    this.editable,
                    this.encrypted,
                    this.systemDefined,
                    this.sortOrder,
                    this.enabled,
                    this.createdBy,
                    this.createdAt,
                    this.updatedBy,
                    this.updatedAt);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
