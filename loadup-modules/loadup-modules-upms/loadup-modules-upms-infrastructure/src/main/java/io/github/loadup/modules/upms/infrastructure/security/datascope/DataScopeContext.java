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

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Scope Context - Holds current user's data scope information
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataScopeContext {

    /** Current user ID */
    private String userId;

    /** Current user's department ID */
    private String deptId;

    /** Data scope type (from role) */
    private DataScopeType dataScopeType;

    /** Custom department IDs (for CUSTOM scope) */
    private List<String> customDeptIds;

    /** Sub-department IDs (for DEPT_AND_SUB scope) */
    private List<String> subDeptIds;

    /** Whether user is super admin (bypass data scope) */
    private boolean isSuperAdmin;

    /** Generate SQL WHERE clause based on data scope */
    public String generateSqlCondition(String deptAlias, String userAlias, String deptIdColumn, String userIdColumn) {
        if (isSuperAdmin || dataScopeType == DataScopeType.ALL) {
            return "1=1"; // No restriction
        }

        switch (dataScopeType) {
            case CUSTOM:
                if (customDeptIds == null || customDeptIds.isEmpty()) {
                    return "1=0"; // No access
                }
                return String.format("%s.%s IN (%s)", deptAlias, deptIdColumn, joinIds(customDeptIds));

            case DEPT:
                if (deptId == null) {
                    return "1=0"; // No access
                }
                return String.format("%s.%s = '%s'", deptAlias, deptIdColumn, deptId);

            case DEPT_AND_SUB:
                if (deptId == null) {
                    return "1=0"; // No access
                }
                if (subDeptIds == null || subDeptIds.isEmpty()) {
                    return String.format("%s.%s = '%s'", deptAlias, deptIdColumn, deptId);
                }
                return String.format("%s.%s IN ('%s',%s)", deptAlias, deptIdColumn, deptId, joinIds(subDeptIds));

            case SELF:
                if (userId == null) {
                    return "1=0"; // No access
                }
                return String.format("%s.%s = '%s'", userAlias, userIdColumn, userId);

            default:
                return "1=0"; // No access by default
        }
    }

    private String joinIds(List<String> ids) {
        return ids.stream()
                .map(id -> "'" + id + "'")
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }
}
