package io.github.loadup.modules.upms.infrastructure.security.datascope;

/*-
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
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

/**
 * Data Scope Types
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public enum DataScopeType {

    /**
     * All data access
     */
    ALL(1, "全部数据权限"),

    /**
     * Custom department selection
     */
    CUSTOM(2, "自定义数据权限"),

    /**
     * Current department only
     */
    DEPT(3, "本部门数据权限"),

    /**
     * Current department and sub-departments
     */
    DEPT_AND_SUB(4, "本部门及以下数据权限"),

    /**
     * Own data only
     */
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
