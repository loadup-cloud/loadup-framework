/*-
 * #%L
 * Testify Spring Boot Starter
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
package io.github.loadup.testify.starter.config;

import io.github.loadup.testify.asserts.engine.DbAssertEngine;
import io.github.loadup.testify.asserts.engine.ExceptionAssertEngine;
import io.github.loadup.testify.asserts.engine.ResponseAssertEngine;
import io.github.loadup.testify.data.engine.function.CommonFunction;
import io.github.loadup.testify.data.engine.function.TimeFunction;
import io.github.loadup.testify.data.engine.variable.VariableEngine;
import io.github.loadup.testify.starter.scenario.ScenarioAssert;
import io.github.loadup.testify.starter.scenario.TestScenario;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration(after = {DataSourceAutoConfiguration.class, JdbcTemplateAutoConfiguration.class})
@ConditionalOnClass(JdbcTemplate.class)
public class TestifyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(JdbcTemplate.class)
    public TestScenario testScenario(JdbcTemplate jdbcTemplate) {
        return new TestScenario(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public VariableEngine testifyVariableEngine() {
        return new VariableEngine(List.of(new TimeFunction(), new CommonFunction()));
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseAssertEngine responseAssertEngine(VariableEngine variableEngine) {
        return new ResponseAssertEngine(variableEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    public DbAssertEngine dbAssertEngine(JdbcTemplate jdbcTemplate, VariableEngine variableEngine) {
        return new DbAssertEngine(jdbcTemplate, variableEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExceptionAssertEngine exceptionAssertEngine(VariableEngine variableEngine) {
        return new ExceptionAssertEngine(variableEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScenarioAssert scenarioAssert(
            ResponseAssertEngine responseAssertEngine,
            DbAssertEngine dbAssertEngine,
            ExceptionAssertEngine exceptionAssertEngine) {
        return new ScenarioAssert(responseAssertEngine, dbAssertEngine, exceptionAssertEngine);
    }
}
