package io.github.loadup.components.database.flyway;

import io.github.loadup.commons.log.LogUtil;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationInitializer;
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
        LogUtil.info(LoadUpFlywayAutoConfiguration.class, ">>> [FLYWAY] Configuring Flyway with LoadUp properties");

        FluentConfiguration config = Flyway.configure().dataSource(dataSource);

        // Migration locations
        if (properties.getLocations() != null && properties.getLocations().length > 0) {
            config.locations(properties.getLocations());
            LogUtil.debug(LoadUpFlywayAutoConfiguration.class, ">>> [FLYWAY] Migration locations: {}", (Object)
                    properties.getLocations());
        }

        // Schema history table: isolates independent version sequences sharing one database
        if (properties.getTable() != null && !properties.getTable().isBlank()) {
            config.table(properties.getTable());
            LogUtil.info(
                    LoadUpFlywayAutoConfiguration.class,
                    ">>> [FLYWAY] Schema history table: {}",
                    properties.getTable());
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
        LogUtil.info(LoadUpFlywayAutoConfiguration.class, ">>> [FLYWAY] Flyway instance configured successfully");
        return flyway;
    }

    /**
     * Registers the migration initializer explicitly.
     *
     * <p>On Spring Boot 4 the whole {@code FlywayConfiguration} (including the migration
     * initializer) is skipped when a custom {@link Flyway} bean already exists. Re-registering the
     * initializer here restores the Boot 3 behavior so migrations run automatically.
     *
     * @param flyway the configured Flyway instance
     * @param migrationStrategy the LoadUp migration strategy
     * @return the migration initializer
     */
    @Bean
    @ConditionalOnMissingBean
    public FlywayMigrationInitializer flywayMigrationInitializer(
            Flyway flyway, FlywayMigrationStrategy migrationStrategy) {
        return new FlywayMigrationInitializer(flyway, migrationStrategy);
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
                LogUtil.info(
                        LoadUpFlywayAutoConfiguration.class,
                        ">>> [FLYWAY] Starting migration (migrate-at-start: true)");
                int applied = flyway.migrate().migrationsExecuted;
                LogUtil.info(
                        LoadUpFlywayAutoConfiguration.class,
                        ">>> [FLYWAY] Migration completed. {} migrations executed",
                        applied);
            } else {
                LogUtil.info(
                        LoadUpFlywayAutoConfiguration.class,
                        ">>> [FLYWAY] migrate-at-start is disabled — skipping automatic migration");
            }
        };
    }
}
