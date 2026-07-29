package io.github.loadup.components.database.flyway;

/*-
 * #%L
 * Loadup Components Flyway
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

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Flyway database migration.
 *
 * <p>Usage example in application.yml:
 * <pre>
 * loadup:
 *   flyway:
 *     enabled: true
 *     locations: classpath:db/migration
 *     baseline-on-migrate: true
 *     validate-on-migrate: true
 *     clean-disabled: true
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "loadup.flyway")
public class FlywayProperties {

    /**
     * Whether to enable Flyway auto-configuration.
     */
    private boolean enabled = true;

    /**
     * Locations of migrations scripts.
     * Can use Spring resource locations (e.g., classpath:db/migration).
     */
    private String[] locations = {"classpath:db/migration"};

    /**
     * Whether to automatically call baseline when migrate is executed against
     * a non-empty schema with no metadata table.
     */
    private boolean baselineOnMigrate = true;

    /**
     * Version to tag an existing schema with when executing baseline.
     */
    private String baselineVersion = "0";

    /**
     * Description to tag an existing schema with when executing baseline.
     */
    private String baselineDescription = "<<Flyway Baseline>>";

    /**
     * Whether to validate migrations.
     */
    private boolean validateOnMigrate = true;

    /**
     * Whether to disable cleaning of the database.
     * Should be true in production to prevent accidental data loss.
     */
    private boolean cleanDisabled = true;

    /**
     * Encoding of SQL migrations.
     */
    private String encoding = "UTF-8";

    /**
     * Placeholders to replace in SQL migrations.
     * Example: ${tableName} will be replaced with the value in this map.
     */
    private Map<String, String> placeholders = new HashMap<>();

    /**
     * Prefix for placeholder in SQL migrations.
     */
    private String placeholderPrefix = "${";

    /**
     * Suffix for placeholder in SQL migrations.
     */
    private String placeholderSuffix = "}";

    /**
     * Whether to enable placeholder replacement.
     */
    private boolean placeholderReplacement = true;

    /**
     * SQL statements to run to initialize a connection immediately after obtaining it.
     */
    private String[] initSqls = {};

    /**
     * Target version up to which migrations should be applied.
     */
    private String target;

    /**
     * Whether to automatically call migrate when the application starts.
     */
    private boolean migrateAtStart = true;

    public FlywayProperties(
            boolean enabled,
            String[] locations,
            boolean baselineOnMigrate,
            String baselineVersion,
            String baselineDescription,
            boolean validateOnMigrate,
            boolean cleanDisabled,
            String encoding,
            Map<String, String> placeholders,
            String placeholderPrefix,
            String placeholderSuffix,
            boolean placeholderReplacement,
            String[] initSqls,
            String target,
            boolean migrateAtStart) {
        this.enabled = enabled;
        this.locations = locations;
        this.baselineOnMigrate = baselineOnMigrate;
        this.baselineVersion = baselineVersion;
        this.baselineDescription = baselineDescription;
        this.validateOnMigrate = validateOnMigrate;
        this.cleanDisabled = cleanDisabled;
        this.encoding = encoding;
        this.placeholders = placeholders;
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
        this.placeholderReplacement = placeholderReplacement;
        this.initSqls = initSqls;
        this.target = target;
        this.migrateAtStart = migrateAtStart;
    }

    public FlywayProperties() {}

    public boolean isEnabled() {
        return this.enabled;
    }

    public String[] getLocations() {
        return this.locations;
    }

    public boolean isBaselineOnMigrate() {
        return this.baselineOnMigrate;
    }

    public String getBaselineVersion() {
        return this.baselineVersion;
    }

    public String getBaselineDescription() {
        return this.baselineDescription;
    }

    public boolean isValidateOnMigrate() {
        return this.validateOnMigrate;
    }

    public boolean isCleanDisabled() {
        return this.cleanDisabled;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public Map<String, String> getPlaceholders() {
        return this.placeholders;
    }

    public String getPlaceholderPrefix() {
        return this.placeholderPrefix;
    }

    public String getPlaceholderSuffix() {
        return this.placeholderSuffix;
    }

    public boolean isPlaceholderReplacement() {
        return this.placeholderReplacement;
    }

    public String[] getInitSqls() {
        return this.initSqls;
    }

    public String getTarget() {
        return this.target;
    }

    public boolean isMigrateAtStart() {
        return this.migrateAtStart;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setLocations(String[] locations) {
        this.locations = locations;
    }

    public void setBaselineOnMigrate(boolean baselineOnMigrate) {
        this.baselineOnMigrate = baselineOnMigrate;
    }

    public void setBaselineVersion(String baselineVersion) {
        this.baselineVersion = baselineVersion;
    }

    public void setBaselineDescription(String baselineDescription) {
        this.baselineDescription = baselineDescription;
    }

    public void setValidateOnMigrate(boolean validateOnMigrate) {
        this.validateOnMigrate = validateOnMigrate;
    }

    public void setCleanDisabled(boolean cleanDisabled) {
        this.cleanDisabled = cleanDisabled;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setPlaceholders(Map<String, String> placeholders) {
        this.placeholders = placeholders;
    }

    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.placeholderPrefix = placeholderPrefix;
    }

    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.placeholderSuffix = placeholderSuffix;
    }

    public void setPlaceholderReplacement(boolean placeholderReplacement) {
        this.placeholderReplacement = placeholderReplacement;
    }

    public void setInitSqls(String[] initSqls) {
        this.initSqls = initSqls;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setMigrateAtStart(boolean migrateAtStart) {
        this.migrateAtStart = migrateAtStart;
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
