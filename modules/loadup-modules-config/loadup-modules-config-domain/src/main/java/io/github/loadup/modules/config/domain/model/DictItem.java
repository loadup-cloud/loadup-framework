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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model for a data dictionary item.
 *
 * <p>Pure POJO — no persistence framework annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictItem {

    private String id;
    private String dictCode;
    private String itemLabel;
    private String itemValue;
    /** Null for top-level items; set for cascaded children. */
    private String parentValue;

    private String cssClass;
    private Integer sortOrder;
    private Boolean enabled;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
