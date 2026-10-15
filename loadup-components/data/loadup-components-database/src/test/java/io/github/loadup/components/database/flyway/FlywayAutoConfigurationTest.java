/*
 * #%L
 * Loadup Components Database
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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
package io.github.loadup.components.database.flyway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationInitializer;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class FlywayAutoConfigurationTest {
    private final DataSource dataSource = mock(DataSource.class);
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LoadUpFlywayAutoConfiguration.class))
            .withBean(DataSource.class, () -> dataSource)
            .withPropertyValues("loadup.flyway.migrate-at-start=false");

    @Test
    void defaultsCreateSafeConfigurationWithoutOpeningDatabaseConnection() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(Flyway.class).hasSingleBean(FlywayMigrationInitializer.class);
            var config = context.getBean(Flyway.class).getConfiguration();
            assertThat(config.getDataSource()).isSameAs(dataSource);
            assertThat(config.getLocations())
                    .extracting(Location::getDescriptor)
                    .containsExactly("classpath:db/migration");
            assertThat(config.getTable()).isEqualTo("flyway_schema_history");
            assertThat(config.isBaselineOnMigrate()).isTrue();
            assertThat(config.getBaselineVersion()).isEqualTo(MigrationVersion.fromVersion("0"));
            assertThat(config.getBaselineDescription()).isEqualTo("<<Flyway Baseline>>");
            assertThat(config.isValidateOnMigrate()).isTrue();
            assertThat(config.isCleanDisabled()).isTrue();
            assertThat(config.getEncoding()).isEqualTo(StandardCharsets.UTF_8);
            assertThat(config.isPlaceholderReplacement()).isTrue();
            assertThat(config.getPlaceholders()).isEmpty();
            assertThat(config.getPlaceholderPrefix()).isEqualTo("${");
            assertThat(config.getPlaceholderSuffix()).isEqualTo("}");
            assertThat(config.getCallbacks()).isEmpty();
            assertThat(context.getBean(FlywayProperties.class).isEnabled()).isTrue();
            verifyNoInteractions(dataSource);
        });
    }

    @Test
    void bindsOperatorOptionsToActualFlywayConfiguration() {
        runner.withPropertyValues(
                        "loadup.flyway.locations=classpath:db/one,classpath:db/two",
                        "loadup.flyway.table=component_history",
                        "loadup.flyway.baseline-on-migrate=false",
                        "loadup.flyway.baseline-version=3",
                        "loadup.flyway.baseline-description=Existing schema",
                        "loadup.flyway.validate-on-migrate=false",
                        "loadup.flyway.clean-disabled=false",
                        "loadup.flyway.encoding=ISO-8859-1",
                        "loadup.flyway.placeholders.tenant=synthetic_tenant",
                        "loadup.flyway.placeholder-prefix=@{",
                        "loadup.flyway.placeholder-suffix=}",
                        "loadup.flyway.placeholder-replacement=false",
                        "loadup.flyway.init-sqls[0]=SET @loadup_test = 1",
                        "loadup.flyway.target=9")
                .run(context -> {
                    var config = context.getBean(Flyway.class).getConfiguration();
                    assertThat(config.getLocations())
                            .extracting(Location::getDescriptor)
                            .containsExactly("classpath:db/one", "classpath:db/two");
                    assertThat(config.getTable()).isEqualTo("component_history");
                    assertThat(config.isBaselineOnMigrate()).isFalse();
                    assertThat(config.getBaselineVersion()).isEqualTo(MigrationVersion.fromVersion("3"));
                    assertThat(config.getBaselineDescription()).isEqualTo("Existing schema");
                    assertThat(config.isValidateOnMigrate()).isFalse();
                    assertThat(config.isCleanDisabled()).isFalse();
                    assertThat(config.getEncoding()).isEqualTo(StandardCharsets.ISO_8859_1);
                    assertThat(config.getPlaceholders()).containsEntry("tenant", "synthetic_tenant");
                    assertThat(config.getPlaceholderPrefix()).isEqualTo("@{");
                    assertThat(config.getPlaceholderSuffix()).isEqualTo("}");
                    assertThat(config.isPlaceholderReplacement()).isFalse();
                    assertThat(config.getCallbacks()).hasSize(1);
                    assertThat(config.getTarget()).isEqualTo(MigrationVersion.fromVersion("9"));
                    verifyNoInteractions(dataSource);
                });
    }

    @Test
    void nullAndEmptyOptionalSettingsPreserveFlywayDefaults() {
        var auto = new LoadUpFlywayAutoConfiguration();
        for (boolean empty : new boolean[] {false, true}) {
            FlywayProperties properties = new FlywayProperties(
                    true,
                    empty ? new String[0] : null,
                    empty ? " " : null,
                    false,
                    null,
                    null,
                    false,
                    true,
                    null,
                    empty ? Map.of() : null,
                    null,
                    null,
                    true,
                    empty ? new String[0] : null,
                    null,
                    false);
            var config = auto.flyway(dataSource, properties).getConfiguration();
            assertThat(config.getLocations())
                    .extracting(Location::getDescriptor)
                    .containsExactly("classpath:db/migration");
            assertThat(config.getTable()).isEqualTo("flyway_schema_history");
            assertThat(config.getCallbacks()).isEmpty();
            assertThat(config.getPlaceholders()).isEmpty();
            assertThat(config.getBaselineVersion()).isEqualTo(MigrationVersion.fromVersion("1"));
            assertThat(config.getPlaceholderPrefix()).isEqualTo("${");
            assertThat(config.getPlaceholderSuffix()).isEqualTo("}");
        }
        verifyNoInteractions(dataSource);
    }

    @Test
    void allNonblankInitializationStatementsRemainRegistered() {
        FlywayProperties properties = new FlywayProperties();
        properties.setInitSqls(new String[] {null, " ", "SET @first = 1", "", "SET @second = 2"});
        var config = new LoadUpFlywayAutoConfiguration()
                .flyway(dataSource, properties)
                .getConfiguration();
        assertThat(config.getCallbacks())
                .extracting(callback -> callback.getCallbackName())
                .containsExactly(
                        new StatementInitCallback("SET @first = 1").getCallbackName(),
                        new StatementInitCallback("SET @second = 2").getCallbackName());
        verifyNoInteractions(dataSource);
    }

    @Test
    void disabledConfigurationExposesNoMigrationBeans() {
        runner.withPropertyValues("loadup.flyway.enabled=false").run(context -> {
            assertThat(context)
                    .doesNotHaveBean(Flyway.class)
                    .doesNotHaveBean(FlywayMigrationInitializer.class)
                    .doesNotHaveBean(FlywayMigrationStrategy.class);
            verifyNoInteractions(dataSource);
        });
    }

    @Test
    void customFlywayAndStrategyArePreservedByInitializer() {
        Flyway flyway = mock(Flyway.class);
        FlywayMigrationStrategy strategy = mock(FlywayMigrationStrategy.class);
        runner.withBean(Flyway.class, () -> flyway)
                .withBean(FlywayMigrationStrategy.class, () -> strategy)
                .run(context -> {
                    assertThat(context.getBean(Flyway.class)).isSameAs(flyway);
                    assertThat(context.getBean(FlywayMigrationStrategy.class)).isSameAs(strategy);
                    assertThat(context).hasSingleBean(FlywayMigrationInitializer.class);
                    verify(strategy).migrate(flyway);
                    verifyNoInteractions(dataSource, flyway);
                });
    }

    @Test
    void migrateAtStartInvokesMigrationAndReportsStartupFailure() {
        Flyway flyway = mock(Flyway.class);
        MigrateResult migrated = new MigrateResult();
        migrated.migrationsExecuted = 2;
        when(flyway.migrate()).thenReturn(migrated);
        runner.withBean(Flyway.class, () -> flyway)
                .withPropertyValues("loadup.flyway.migrate-at-start=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    verify(flyway).migrate();
                });
        Flyway failed = mock(Flyway.class);
        FlywayException failure = new FlywayException("synthetic migration failure");
        when(failed.migrate()).thenThrow(failure);
        runner.withBean(Flyway.class, () -> failed)
                .withPropertyValues("loadup.flyway.migrate-at-start=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCause(failure);
                });
    }
}
