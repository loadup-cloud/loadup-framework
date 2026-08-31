package io.github.loadup.modules.config.client.command;

/*-
 * #%L
 * Loadup Modules Config Client
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
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
