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
import lombok.Data;

/**
 * Command to create a config item.
 *
 * @author LoadUp Framework
 */
@Data
public class ConfigItemCreateCommand {

    @NotBlank(message = "配置键不能为空")
    @Size(max = 200, message = "配置键长度不能超过200")
    private String configKey;

    private String configValue;

    /** STRING / INTEGER / LONG / DOUBLE / BOOLEAN / JSON */
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
}
