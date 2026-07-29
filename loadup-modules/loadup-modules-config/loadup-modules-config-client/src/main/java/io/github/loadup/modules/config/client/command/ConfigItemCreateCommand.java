package io.github.loadup.modules.config.client.command;

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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Command to create a config item.
 *
 * @author LoadUp Framework
 */
public class ConfigItemCreateCommand {

    @NotBlank(message = "配置键不能为空")
    @Size(max = 200, message = "配置键长度不能超过200")
    private String configKey;

    private String configValue;

    /**
     * STRING / INTEGER / LONG / DOUBLE / BOOLEAN / JSON
     */
    @NotBlank(message = "值类型不能为空")
    private String valueType;

    @NotBlank(message = "分类不能为空")
    @Size(max = 50)
    private String category;

    @Size(max = 500)
    private String description;

    private Boolean editable = Boolean.TRUE;
    private Boolean encrypted = Boolean.FALSE;
    private Integer sortOrder = 0;

    public ConfigItemCreateCommand(
            String configKey,
            String configValue,
            String valueType,
            String category,
            String description,
            Boolean editable,
            Boolean encrypted,
            Integer sortOrder) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.valueType = valueType;
        this.category = category;
        this.description = description;
        this.editable = editable;
        this.encrypted = encrypted;
        this.sortOrder = sortOrder;
    }

    public ConfigItemCreateCommand() {}

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

    public Integer getSortOrder() {
        return this.sortOrder;
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

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                configKey, configValue, valueType, category, description, editable, encrypted, sortOrder);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigItemCreateCommand other = (ConfigItemCreateCommand) o;
        if (!java.util.Objects.equals(configKey, other.configKey)) return false;
        if (!java.util.Objects.equals(configValue, other.configValue)) return false;
        if (!java.util.Objects.equals(valueType, other.valueType)) return false;
        if (!java.util.Objects.equals(category, other.category)) return false;
        if (!java.util.Objects.equals(description, other.description)) return false;
        if (!java.util.Objects.equals(editable, other.editable)) return false;
        if (!java.util.Objects.equals(encrypted, other.encrypted)) return false;
        if (!java.util.Objects.equals(sortOrder, other.sortOrder)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "ConfigItemCreateCommand(" + "configKey=" + configKey + ", " + "configValue=" + configValue + ", "
                + "valueType=" + valueType + ", " + "category=" + category + ", " + "description=" + description + ", "
                + "editable=" + editable + ", " + "encrypted=" + encrypted + ", " + "sortOrder=" + sortOrder + ")";
    }
}
