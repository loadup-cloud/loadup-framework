package io.github.loadup.components.database.config;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

    public DatabaseProperties() {}

    public MultiTenant getMultiTenant() {
        return this.multiTenant;
    }

    public LogicalDelete getLogicalDelete() {
        return this.logicalDelete;
    }
}
