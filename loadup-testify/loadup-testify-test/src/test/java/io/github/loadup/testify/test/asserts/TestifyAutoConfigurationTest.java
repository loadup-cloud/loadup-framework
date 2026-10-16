/*
 * #%L
 * Testify Demo
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
package io.github.loadup.testify.test.asserts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.testify.asserts.engine.DbAssertEngine;
import io.github.loadup.testify.asserts.engine.ExceptionAssertEngine;
import io.github.loadup.testify.asserts.engine.ResponseAssertEngine;
import io.github.loadup.testify.data.engine.variable.VariableEngine;
import io.github.loadup.testify.starter.config.TestifyAutoConfiguration;
import io.github.loadup.testify.starter.scenario.ScenarioAssert;
import io.github.loadup.testify.starter.scenario.TestScenario;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;

class TestifyAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(TestifyAutoConfiguration.class));

    @Test
    void nonDatabaseAssertionsRemainAvailableWithoutJdbcTemplate() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(VariableEngine.class);
            assertThat(context).hasSingleBean(ResponseAssertEngine.class);
            assertThat(context).hasSingleBean(ExceptionAssertEngine.class);
            assertThat(context).hasSingleBean(ScenarioAssert.class);
            assertThat(context).doesNotHaveBean(TestScenario.class);
            assertThat(context).doesNotHaveBean(DbAssertEngine.class);

            ScenarioAssert assertions = context.getBean(ScenarioAssert.class);
            assertions.verify(Map.of("id", "u-1"), Map.of("id", "u-1"));
            assertions.verifyException(
                    new IllegalArgumentException("invalid"),
                    Map.of("type", "IllegalArgumentException", "message", "invalid"));
            assertThatThrownBy(() -> assertions.verifyDb(Map.of("table", "users")))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("JdbcTemplate bean");
        });
    }

    @Test
    void databaseCapabilitiesAreRegisteredWithJdbcTemplate() {
        contextRunner.withBean(JdbcTemplate.class, JdbcTemplate::new).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(TestScenario.class);
            assertThat(context).hasSingleBean(DbAssertEngine.class);
            assertThat(context).hasSingleBean(ScenarioAssert.class);
            context.getBean(ScenarioAssert.class).verify(Map.of("id", "u-1"), Map.of("id", "u-1"));
        });
    }
}
