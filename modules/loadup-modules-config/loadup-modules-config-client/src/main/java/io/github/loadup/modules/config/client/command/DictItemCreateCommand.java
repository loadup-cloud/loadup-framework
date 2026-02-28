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
 * Command to create a dict item.
 */
@Data
public class DictItemCreateCommand {

    @NotBlank
    private String dictCode;

    @NotBlank
    @Size(max = 200)
    private String itemLabel;

    @NotBlank
    @Size(max = 200)
    private String itemValue;

    /** Parent value for cascaded dict. */
    private String parentValue;

    @Size(max = 100)
    private String cssClass;

    private Integer sortOrder = 0;
}

