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

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for Flyway database migrations.
 *
 * <p>This configuration provides enhanced Flyway integration for LoadUp Framework,
 * building on top of Spring Boot's default Flyway support.
 *
 * <p>Features:
 * <ul>
 *   <li>Additional configuration properties under loadup.flyway prefix</li>
 *   <li>Automatic migration on startup (configurable)</li>
 *   <li>Support for placeholders and custom SQL initialization</li>
 * </ul>
 *
 * <p>Configuration example:
 * <pre>
 * loadup:
 *   flyway:
 *     enabled: true
 *     locations: classpath:db/migration
 *     baseline-on-migrate: true
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration(before = FlywayAutoConfiguration.class)
@ConditionalOnClass(Flyway.class)
@ConditionalOnProperty(prefix = "loadup.flyway", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FlywayProperties.class)
public class LoadUpFlywayAutoConfiguration {

    /**
     * Configure Flyway with LoadUp-specific properties.
     *
     * <p>This customizer runs before Spring Boot's default Flyway configuration,
     * allowing us to apply custom settings from loadup.flyway.* properties.
     *
     * @param properties LoadUp Flyway properties
     * @return Flyway configuration customizer
     */
    @Bean
    public org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
            loadupFlywayConfigurationCustomizer(FlywayProperties properties) {

        return configuration -> {
            log.info(">>> [FLYWAY] Configuring Flyway with LoadUp properties");

            // Locations
            if (properties.getLocations() != null && properties.getLocations().length > 0) {
                configuration.locations(properties.getLocations());
                log.debug(">>> [FLYWAY] Migration locations: {}", (Object) properties.getLocations());
            }

            // Baseline configuration
            configuration.baselineOnMigrate(properties.isBaselineOnMigrate());
            configuration.baselineVersion(properties.getBaselineVersion());
            configuration.baselineDescription(properties.getBaselineDescription());

            // Validation
            configuration.validateOnMigrate(properties.isValidateOnMigrate());

            // Clean (should be disabled in production)
            configuration.cleanDisabled(properties.isCleanDisabled());

            // Encoding
            if (properties.getEncoding() != null) {
                configuration.encoding(java.nio.charset.Charset.forName(properties.getEncoding()));
            }

            // Placeholders
            if (properties.getPlaceholders() != null
                    && !properties.getPlaceholders().isEmpty()) {
                configuration.placeholders(properties.getPlaceholders());
                configuration.placeholderReplacement(properties.isPlaceholderReplacement());
                if (properties.getPlaceholderPrefix() != null) {
                    configuration.placeholderPrefix(properties.getPlaceholderPrefix());
                }
                if (properties.getPlaceholderSuffix() != null) {
                    configuration.placeholderSuffix(properties.getPlaceholderSuffix());
                }
            }

            // Init SQLs
            if (properties.getInitSqls() != null && properties.getInitSqls().length > 0) {
                configuration.initSql(String.join(";", properties.getInitSqls()));
            }

            // Target version
            if (properties.getTarget() != null) {
                configuration.target(org.flywaydb.core.api.MigrationVersion.fromVersion(properties.getTarget()));
            }

            log.info(">>> [FLYWAY] Configuration completed");
        };
    }

    /**
     * Configuration for migration execution on startup.
     */
    @Configuration
    @ConditionalOnProperty(
            prefix = "loadup.flyway",
            name = "migrate-at-start",
            havingValue = "true",
            matchIfMissing = true)
    static class FlywayMigrationConfiguration {

        /**
         * Execute Flyway migration on application startup.
         *
         * @param flyway Flyway instance
         * @param dataSource DataSource
         */
        @Bean
        public FlywayMigrationInitializer flywayMigrationInitializer(Flyway flyway, DataSource dataSource) {

            log.info(">>> [FLYWAY] Executing database migrations on startup");
            return new FlywayMigrationInitializer(flyway);
        }
    }

    /**
     * Initializer that triggers Flyway migration.
     */
    static class FlywayMigrationInitializer {

        public FlywayMigrationInitializer(Flyway flyway) {
            try {
                log.info(">>> [FLYWAY] Starting migration...");
                int migrationsApplied = flyway.migrate().migrationsExecuted;
                log.info(">>> [FLYWAY] Migration completed. {} migrations applied", migrationsApplied);
            } catch (Exception e) {
                log.error(">>> [FLYWAY] Migration failed: {}", e.getMessage(), e);
                throw new RuntimeException("Flyway migration failed", e);
            }
        }
    }
}
