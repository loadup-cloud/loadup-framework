package io.github.loadup.components.database.config;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for database component.
 */
@ConfigurationProperties(prefix = "loadup.database")
public class DatabaseProperties {

    /**
     * Multi-tenant configuration
     */
    private MultiTenant multiTenant = new MultiTenant();

    /**
     * Logical delete configuration
     */
    private LogicalDelete logicalDelete = new LogicalDelete();

    public static class MultiTenant {
        /**
         * Enable multi-tenant feature (default: false)
         */
        private boolean enabled = false;

        /**
         * Column name for tenant ID (default: tenant_id)
         */
        private String columnName = "tenant_id";

        /**
         * Ignore tenant filter for these tables (comma separated)
         */
        private String ignoreTables = "sys_tenant,sys_user,sys_role,sys_permission";

        /**
         * Default tenant ID when not in tenant context
         */
        private String defaultTenantId = "default";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getIgnoreTables() {
            return ignoreTables;
        }

        public void setIgnoreTables(String ignoreTables) {
            this.ignoreTables = ignoreTables;
        }

        public String getDefaultTenantId() {
            return defaultTenantId;
        }

        public void setDefaultTenantId(String defaultTenantId) {
            this.defaultTenantId = defaultTenantId;
        }
    }

    public static class LogicalDelete {
        /**
         * Enable logical delete feature (default: false)
         */
        private boolean enabled = false;

        /**
         * Column name for logical delete flag (default: deleted)
         */
        private String columnName = "deleted";

        /**
         * Value representing deleted record (default: true)
         */
        private String deletedValue = "true";

        /**
         * Value representing normal record (default: false)
         */
        private String normalValue = "false";

        public String getNormalValue() {
            return normalValue;
        }

        public void setNormalValue(String normalValue) {
            this.normalValue = normalValue;
        }

        public String getDeletedValue() {
            return deletedValue;
        }

        public void setDeletedValue(String deletedValue) {
            this.deletedValue = deletedValue;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }


    public DatabaseProperties() {
    }

    public MultiTenant getMultiTenant() {
        return this.multiTenant;
    }

    public LogicalDelete getLogicalDelete() {
        return this.logicalDelete;
    }


}
