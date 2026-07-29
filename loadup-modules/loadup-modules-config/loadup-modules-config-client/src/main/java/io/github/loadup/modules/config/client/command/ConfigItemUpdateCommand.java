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
 * Command to update a config item value.
 *
 * @author LoadUp Framework
 */
public class ConfigItemUpdateCommand {

    @NotBlank(message = "配置键不能为空")
    private String configKey;

    private String configValue;

    @Size(max = 500)
    private String remark;

    public ConfigItemUpdateCommand(String configKey, String configValue, String remark) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.remark = remark;
    }

    public ConfigItemUpdateCommand() {}

    public String getConfigKey() {
        return this.configKey;
    }

    public String getConfigValue() {
        return this.configValue;
    }

    public String getRemark() {
        return this.remark;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
