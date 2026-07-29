package io.github.loadup.modules.config.client.dto;

/*-
 * #%L
 * Loadup Modules Config Client
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
 * System configuration item DTO.
 *
 * @author LoadUp Framework
 */
public class ConfigItemDTO {

    private String id;
    private String configKey;
    /**
     * Raw string value (decrypted if encrypted)
     */
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
    private LocalDateTime updatedAt;

    public ConfigItemDTO(String id, String configKey, String configValue, String valueType, String category, String description, Boolean editable, Boolean encrypted, Boolean systemDefined, Integer sortOrder, Boolean enabled, LocalDateTime updatedAt) {
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
        this.updatedAt = updatedAt;
    }

    public ConfigItemDTO() {
    }

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

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, configKey, configValue, valueType, category, description, editable, encrypted, systemDefined, sortOrder, enabled, updatedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigItemDTO other = (ConfigItemDTO) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(configKey, other.configKey)) return false;
        if (!java.util.Objects.equals(configValue, other.configValue)) return false;
        if (!java.util.Objects.equals(valueType, other.valueType)) return false;
        if (!java.util.Objects.equals(category, other.category)) return false;
        if (!java.util.Objects.equals(description, other.description)) return false;
        if (!java.util.Objects.equals(editable, other.editable)) return false;
        if (!java.util.Objects.equals(encrypted, other.encrypted)) return false;
        if (!java.util.Objects.equals(systemDefined, other.systemDefined)) return false;
        if (!java.util.Objects.equals(sortOrder, other.sortOrder)) return false;
        if (!java.util.Objects.equals(enabled, other.enabled)) return false;
        if (!java.util.Objects.equals(updatedAt, other.updatedAt)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "ConfigItemDTO(" + "id=" + id + ", " + "configKey=" + configKey + ", " + "configValue=" + configValue + ", " + "valueType=" + valueType + ", " + "category=" + category + ", " + "description=" + description + ", " + "editable=" + editable + ", " + "encrypted=" + encrypted + ", " + "systemDefined=" + systemDefined + ", " + "sortOrder=" + sortOrder + ", " + "enabled=" + enabled + ", " + "updatedAt=" + updatedAt + ")";
    }
}
