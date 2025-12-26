/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.framework.liquibase.config;

/*-
 * #%L
 * loadup-components-liquibase
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Liquibase configuration properties
 *
 * @author Lise
 */
@ConfigurationProperties(prefix = "loadup.liquibase")
public class LiquibaseProperties {

    /**
     * Whether to enable Liquibase
     */
    private boolean enabled = true;

    /**
     * Change log configuration path
     */
    private String changeLog = "classpath:db/changelog/db.changelog-master.yaml";

    /**
     * Contexts to use
     */
    private String contexts;

    /**
     * Labels to use for filtering change sets
     */
    private String labels;

    /**
     * Default database schema
     */
    private String defaultSchema;

    /**
     * Schema to use for Liquibase objects
     */
    private String liquibaseSchema;

    /**
     * Tablespace to use for Liquibase objects
     */
    private String liquibaseTablespace;

    /**
     * Name of table to use for tracking change history
     */
    private String databaseChangeLogTable = "DATABASECHANGELOG";

    /**
     * Name of table to use for tracking concurrent Liquibase usage
     */
    private String databaseChangeLogLockTable = "DATABASECHANGELOGLOCK";

    /**
     * Whether to drop the database schema first
     */
    private boolean dropFirst = false;

    /**
     * Whether to clear all checksums in the current changelog
     */
    private boolean clearChecksums = false;

    /**
     * Whether to perform a tag operation on the database
     */
    private String tag;

    /**
     * Test rollback tag
     */
    private String testRollbackOnUpdate;

    /**
     * Whether to enable validate checksums
     */
    private boolean validateOnMigrate = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    public String getContexts() {
        return contexts;
    }

    public void setContexts(String contexts) {
        this.contexts = contexts;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }

    public String getLiquibaseSchema() {
        return liquibaseSchema;
    }

    public void setLiquibaseSchema(String liquibaseSchema) {
        this.liquibaseSchema = liquibaseSchema;
    }

    public String getLiquibaseTablespace() {
        return liquibaseTablespace;
    }

    public void setLiquibaseTablespace(String liquibaseTablespace) {
        this.liquibaseTablespace = liquibaseTablespace;
    }

    public String getDatabaseChangeLogTable() {
        return databaseChangeLogTable;
    }

    public void setDatabaseChangeLogTable(String databaseChangeLogTable) {
        this.databaseChangeLogTable = databaseChangeLogTable;
    }

    public String getDatabaseChangeLogLockTable() {
        return databaseChangeLogLockTable;
    }

    public void setDatabaseChangeLogLockTable(String databaseChangeLogLockTable) {
        this.databaseChangeLogLockTable = databaseChangeLogLockTable;
    }

    public boolean isDropFirst() {
        return dropFirst;
    }

    public void setDropFirst(boolean dropFirst) {
        this.dropFirst = dropFirst;
    }

    public boolean isClearChecksums() {
        return clearChecksums;
    }

    public void setClearChecksums(boolean clearChecksums) {
        this.clearChecksums = clearChecksums;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTestRollbackOnUpdate() {
        return testRollbackOnUpdate;
    }

    public void setTestRollbackOnUpdate(String testRollbackOnUpdate) {
        this.testRollbackOnUpdate = testRollbackOnUpdate;
    }

    public boolean isValidateOnMigrate() {
        return validateOnMigrate;
    }

    public void setValidateOnMigrate(boolean validateOnMigrate) {
        this.validateOnMigrate = validateOnMigrate;
    }
}

