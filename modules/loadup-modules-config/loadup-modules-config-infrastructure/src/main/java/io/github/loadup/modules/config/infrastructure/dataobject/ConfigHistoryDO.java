package io.github.loadup.modules.config.infrastructure.dataobject;

/*-
 * #%L
 * Loadup Modules Config Infrastructure
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

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import lombok.*;

/**
 * config_history 表映射对象。
 *
 * <p>历史记录只追加不修改，不继承 BaseDO（无 updatedAt/tenantId/deleted 字段需求）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table("config_history")
public class ConfigHistoryDO extends BaseDO {

    private String configKey;
    private String oldValue;
    private String newValue;
    /**
     * CREATE / UPDATE / DELETE
     */
    private String changeType;

    private String operator;
    private String remark;
}
