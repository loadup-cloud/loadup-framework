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
