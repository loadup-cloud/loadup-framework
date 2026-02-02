package io.github.loadup.modules.upms.infrastructure.security.datascope;

/*-
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
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

/**
 * Data Scope Types
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public enum DataScopeType {

    /** All data access */
    ALL(1, "全部数据权限"),

    /** Custom department selection */
    CUSTOM(2, "自定义数据权限"),

    /** Current department only */
    DEPT(3, "本部门数据权限"),

    /** Current department and sub-departments */
    DEPT_AND_SUB(4, "本部门及以下数据权限"),

    /** Own data only */
    SELF(5, "仅本人数据权限");

    private final int code;
    private final String description;

    DataScopeType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static DataScopeType fromCode(int code) {
        for (DataScopeType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown data scope type code: " + code);
    }
}
