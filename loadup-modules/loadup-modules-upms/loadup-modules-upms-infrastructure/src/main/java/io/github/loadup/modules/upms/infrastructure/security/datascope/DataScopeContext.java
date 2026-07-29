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

/**
 * Data Scope Context - Holds current user's data scope information
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class DataScopeContext {

    /**
     * Current user ID
     */
    private String userId;

    /**
     * Current user's department ID
     */
    private String deptId;

    /**
     * Data scope type (from role)
     */
    private DataScopeType dataScopeType;

    /**
     * Custom department IDs (for CUSTOM scope)
     */
    private List<String> customDeptIds;

    /**
     * Sub-department IDs (for DEPT_AND_SUB scope)
     */
    private List<String> subDeptIds;

    /**
     * Whether user is super admin (bypass data scope)
     */
    private boolean isSuperAdmin;

    /**
     * Generate SQL WHERE clause based on data scope
     */
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

    public DataScopeContext(
            String userId,
            String deptId,
            DataScopeType dataScopeType,
            List<String> customDeptIds,
            List<String> subDeptIds,
            boolean isSuperAdmin) {
        this.userId = userId;
        this.deptId = deptId;
        this.dataScopeType = dataScopeType;
        this.customDeptIds = customDeptIds;
        this.subDeptIds = subDeptIds;
        this.isSuperAdmin = isSuperAdmin;
    }

    public DataScopeContext() {}

    public String getUserId() {
        return this.userId;
    }

    public String getDeptId() {
        return this.deptId;
    }

    public DataScopeType getDataScopeType() {
        return this.dataScopeType;
    }

    public List<String> getCustomDeptIds() {
        return this.customDeptIds;
    }

    public List<String> getSubDeptIds() {
        return this.subDeptIds;
    }

    public boolean isIsSuperAdmin() {
        return this.isSuperAdmin;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public void setDataScopeType(DataScopeType dataScopeType) {
        this.dataScopeType = dataScopeType;
    }

    public void setCustomDeptIds(List<String> customDeptIds) {
        this.customDeptIds = customDeptIds;
    }

    public void setSubDeptIds(List<String> subDeptIds) {
        this.subDeptIds = subDeptIds;
    }

    public void setIsSuperAdmin(boolean isSuperAdmin) {
        this.isSuperAdmin = isSuperAdmin;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userId;
        private String deptId;
        private DataScopeType dataScopeType;
        private List<String> customDeptIds;
        private List<String> subDeptIds;
        private boolean isSuperAdmin;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder deptId(String deptId) {
            this.deptId = deptId;
            return this;
        }

        public Builder dataScopeType(DataScopeType dataScopeType) {
            this.dataScopeType = dataScopeType;
            return this;
        }

        public Builder customDeptIds(List<String> customDeptIds) {
            this.customDeptIds = customDeptIds;
            return this;
        }

        public Builder subDeptIds(List<String> subDeptIds) {
            this.subDeptIds = subDeptIds;
            return this;
        }

        public Builder isSuperAdmin(boolean isSuperAdmin) {
            this.isSuperAdmin = isSuperAdmin;
            return this;
        }

        public DataScopeContext build() {
            return new DataScopeContext(
                    this.userId,
                    this.deptId,
                    this.dataScopeType,
                    this.customDeptIds,
                    this.subDeptIds,
                    this.isSuperAdmin);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
