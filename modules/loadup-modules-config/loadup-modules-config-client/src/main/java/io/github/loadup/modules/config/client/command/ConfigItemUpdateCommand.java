package io.github.loadup.modules.config.client.command;

/*-
 * #%L
 * Loadup Modules Config Client
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * #L%
 */

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Command to update a config item value.
 *
 * @author LoadUp Framework
 */
@Data
public class ConfigItemUpdateCommand {

    @NotBlank(message = "配置键不能为空")
    private String configKey;

    private String configValue;

    @Size(max = 500)
    private String remark;
}

