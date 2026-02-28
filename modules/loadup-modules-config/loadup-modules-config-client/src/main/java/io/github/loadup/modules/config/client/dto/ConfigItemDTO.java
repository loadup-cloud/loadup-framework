package io.github.loadup.modules.config.client.dto;

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

import java.time.LocalDateTime;
import lombok.Data;

/**
 * System configuration item DTO.
 *
 * @author LoadUp Framework
 */
@Data
public class ConfigItemDTO {

    private String id;
    private String configKey;
    /** Raw string value (decrypted if encrypted) */
    private String configValue;
    /** STRING / INTEGER / LONG / DOUBLE / BOOLEAN / JSON */
    private String valueType;
    private String category;
    private String description;
    private Boolean editable;
    private Boolean encrypted;
    private Boolean systemDefined;
    private Integer sortOrder;
    private Boolean enabled;
    private LocalDateTime updatedAt;
}

