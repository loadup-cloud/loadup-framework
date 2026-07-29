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

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Flyway database migrations.
 *
 * <p>Provides enhanced Flyway integration for the LoadUp Framework,
 * with additional configuration properties under the {@code loadup.flyway} prefix.
 * Registers its {@link Flyway} bean before Spring Boot's own
 * {@link FlywayAutoConfiguration} so the LoadUp-specific configuration
 * takes precedence.
 *
 * <p>Configuration example:
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
@AutoConfiguration(before = FlywayAutoConfiguration.class)
@ConditionalOnClass(Flyway.class)
@ConditionalOnProperty(prefix = "loadup.flyway", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FlywayProperties.class)
public class LoadUpFlywayAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(LoadUpFlywayAutoConfiguration.class);

    /**
     * Create the Flyway bean with LoadUp-specific configuration.
     *
     * <p>Registered before Spring Boot's own {@link FlywayAutoConfiguration}
     * so our configuration takes precedence via {@link ConditionalOnMissingBean}.
     *
     * @param dataSource the primary DataSource
     * @param properties LoadUp Flyway properties
     * @return configured Flyway instance
     */
    @Bean
    @ConditionalOnMissingBean
    public Flyway flyway(DataSource dataSource, FlywayProperties properties) {
        log.info(">>> [FLYWAY] Configuring Flyway with LoadUp properties");

        FluentConfiguration config = Flyway.configure().dataSource(dataSource);

        // Migration locations
        if (properties.getLocations() != null && properties.getLocations().length > 0) {
            config.locations(properties.getLocations());
            log.debug(">>> [FLYWAY] Migration locations: {}", (Object) properties.getLocations());
        }

        // Baseline configuration
        config.baselineOnMigrate(properties.isBaselineOnMigrate());
        if (properties.getBaselineVersion() != null) {
            config.baselineVersion(properties.getBaselineVersion());
        }
        if (properties.getBaselineDescription() != null) {
            config.baselineDescription(properties.getBaselineDescription());
        }

        // Validation
        config.validateOnMigrate(properties.isValidateOnMigrate());

        // Clean disabled (always true in production)
        config.cleanDisabled(properties.isCleanDisabled());

        // Encoding
        if (properties.getEncoding() != null) {
            config.encoding(java.nio.charset.Charset.forName(properties.getEncoding()));
        }

        // Placeholders
        if (properties.getPlaceholders() != null
                && !properties.getPlaceholders().isEmpty()) {
            config.placeholders(properties.getPlaceholders());
        }
        config.placeholderReplacement(properties.isPlaceholderReplacement());
        if (properties.getPlaceholderPrefix() != null) {
            config.placeholderPrefix(properties.getPlaceholderPrefix());
        }
        if (properties.getPlaceholderSuffix() != null) {
            config.placeholderSuffix(properties.getPlaceholderSuffix());
        }

        // Init SQLs — applied via statement-scoped callback on connect
        if (properties.getInitSqls() != null && properties.getInitSqls().length > 0) {
            for (String sql : properties.getInitSqls()) {
                if (sql != null && !sql.isBlank()) {
                    config.callbacks(new StatementInitCallback(sql));
                }
            }
        }

        // Target version
        if (properties.getTarget() != null) {
            config.target(MigrationVersion.fromVersion(properties.getTarget()));
        }

        Flyway flyway = config.load();
        log.info(">>> [FLYWAY] Flyway instance configured successfully");
        return flyway;
    }

    /**
     * Provide a migration strategy that respects the {@code migrate-at-start} property.
     *
     * <p>When enabled, migrations run on application startup. When disabled,
     * the Flyway bean is created but no automatic migration occurs — useful for
     * environments where migrations are run manually or by an external process.
     *
     * @param properties LoadUp Flyway properties
     * @return FlywayMigrationStrategy
     */
    @Bean
    @ConditionalOnMissingBean
    public FlywayMigrationStrategy loadupFlywayMigrationStrategy(FlywayProperties properties) {
        return flyway -> {
            if (properties.isMigrateAtStart()) {
                log.info(">>> [FLYWAY] Starting migration (migrate-at-start: true)");
                int applied = flyway.migrate().migrationsExecuted;
                log.info(">>> [FLYWAY] Migration completed. {} migrations executed", applied);
            } else {
                log.info(">>> [FLYWAY] migrate-at-start is disabled — skipping automatic migration");
            }
        };
    }
}
