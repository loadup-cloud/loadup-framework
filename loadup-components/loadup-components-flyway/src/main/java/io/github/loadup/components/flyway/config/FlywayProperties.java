package io.github.loadup.components.flyway.config;

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

import lombok.Data;
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
@Data
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
    private java.util.Map<String, String> placeholders = new java.util.HashMap<>();

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
}
