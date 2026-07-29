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
    }

    public DatabaseProperties(MultiTenant multiTenant, LogicalDelete logicalDelete, boolean enabled, String columnName, String ignoreTables, String defaultTenantId, boolean enabled, String columnName, String deletedValue, String normalValue) {
        this.multiTenant = multiTenant;
        this.logicalDelete = logicalDelete;
        this.enabled = enabled;
        this.columnName = columnName;
        this.ignoreTables = ignoreTables;
        this.defaultTenantId = defaultTenantId;
        this.enabled = enabled;
        this.columnName = columnName;
        this.deletedValue = deletedValue;
        this.normalValue = normalValue;
    }

    public DatabaseProperties() {
    }

    public MultiTenant getMultiTenant() {
        return this.multiTenant;
    }

    public LogicalDelete getLogicalDelete() {
        return this.logicalDelete;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public String getIgnoreTables() {
        return this.ignoreTables;
    }

    public String getDefaultTenantId() {
        return this.defaultTenantId;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public String getDeletedValue() {
        return this.deletedValue;
    }

    public String getNormalValue() {
        return this.normalValue;
    }

    public void setMultiTenant(MultiTenant multiTenant) {
        this.multiTenant = multiTenant;
    }

    public void setLogicalDelete(LogicalDelete logicalDelete) {
        this.logicalDelete = logicalDelete;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setIgnoreTables(String ignoreTables) {
        this.ignoreTables = ignoreTables;
    }

    public void setDefaultTenantId(String defaultTenantId) {
        this.defaultTenantId = defaultTenantId;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setDeletedValue(String deletedValue) {
        this.deletedValue = deletedValue;
    }

    public void setNormalValue(String normalValue) {
        this.normalValue = normalValue;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(multiTenant, logicalDelete, enabled, columnName, ignoreTables, defaultTenantId, enabled, columnName, deletedValue, normalValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseProperties other = (DatabaseProperties) o;
        if (!java.util.Objects.equals(multiTenant, other.multiTenant)) return false;
        if (!java.util.Objects.equals(logicalDelete, other.logicalDelete)) return false;
        if (!java.util.Objects.equals(enabled, other.enabled)) return false;
        if (!java.util.Objects.equals(columnName, other.columnName)) return false;
        if (!java.util.Objects.equals(ignoreTables, other.ignoreTables)) return false;
        if (!java.util.Objects.equals(defaultTenantId, other.defaultTenantId)) return false;
        if (!java.util.Objects.equals(enabled, other.enabled)) return false;
        if (!java.util.Objects.equals(columnName, other.columnName)) return false;
        if (!java.util.Objects.equals(deletedValue, other.deletedValue)) return false;
        if (!java.util.Objects.equals(normalValue, other.normalValue)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "DatabaseProperties(" + "multiTenant=" + multiTenant + ", " + "logicalDelete=" + logicalDelete + ", " + "enabled=" + enabled + ", " + "columnName=" + columnName + ", " + "ignoreTables=" + ignoreTables + ", " + "defaultTenantId=" + defaultTenantId + ", " + "enabled=" + enabled + ", " + "columnName=" + columnName + ", " + "deletedValue=" + deletedValue + ", " + "normalValue=" + normalValue + ")";
    }
}
