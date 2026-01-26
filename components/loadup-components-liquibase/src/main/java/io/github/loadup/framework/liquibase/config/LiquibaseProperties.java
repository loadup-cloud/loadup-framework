package io.github.loadup.framework.liquibase.config;

/*-
 * #%L
 * loadup-components-liquibase
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
 * Liquibase configuration properties
 *
 * @author Lise
 */
@ConfigurationProperties(prefix = "loadup.liquibase")
public class LiquibaseProperties {

  /** Whether to enable Liquibase */
  private boolean enabled = true;

  /** Change log configuration path */
  private String changeLog = "classpath:db/changelog/db.changelog-master.yaml";

  /** Contexts to use */
  private String contexts;

  /** Labels to use for filtering change sets */
  private String labels;

  /** Default database schema */
  private String defaultSchema;

  /** Schema to use for Liquibase objects */
  private String liquibaseSchema;

  /** Tablespace to use for Liquibase objects */
  private String liquibaseTablespace;

  /** Name of table to use for tracking change history */
  private String databaseChangeLogTable = "DATABASECHANGELOG";

  /** Name of table to use for tracking concurrent Liquibase usage */
  private String databaseChangeLogLockTable = "DATABASECHANGELOGLOCK";

  /** Whether to drop the database schema first */
  private boolean dropFirst = false;

  /** Whether to clear all checksums in the current changelog */
  private boolean clearChecksums = false;

  /** Whether to perform a tag operation on the database */
  private String tag;

  /** Test rollback tag */
  private String testRollbackOnUpdate;

  /** Whether to enable validate checksums */
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
