package io.github.loadup.commons.log.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.commons.log.LogContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class LoadupLogEnvironmentPostProcessorTest {

    private final LoadupLogEnvironmentPostProcessor processor = new LoadupLogEnvironmentPostProcessor();

    @Test
    void addsDefaultConsolePattern() {
        MockEnvironment environment = new MockEnvironment();

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("logging.pattern.console")).isEqualTo(LogContext.DEFAULT_CONSOLE_PATTERN);
        assertThat(environment.getProperty("logging.pattern.file")).isEqualTo(LogContext.DEFAULT_CONSOLE_PATTERN);
    }

    @Test
    void applicationPatternHasHigherPriorityThanDefault() {
        MockEnvironment environment = new MockEnvironment().withProperty("logging.pattern.console", "custom");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("logging.pattern.console")).isEqualTo("custom");
    }

    @Test
    void canDisableTraceFieldsInDefaultPattern() {
        MockEnvironment environment = new MockEnvironment().withProperty("loadup.log.include-trace-context", "false");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("logging.pattern.console"))
                .isEqualTo(LogContext.DEFAULT_CONSOLE_PATTERN_WITHOUT_TRACE);
    }

    @Test
    void filePatternCanBeConfiguredAndBootSettingsWin() {
        var environment = new MockEnvironment()
                .withProperty("loadup.log.file-pattern", "file-default")
                .withProperty("logging.pattern.console", "console-override");
        processor.postProcessEnvironment(environment, new SpringApplication());
        assertThat(environment.getProperty("logging.pattern.file")).isEqualTo("file-default");
        assertThat(environment.getProperty("logging.pattern.console")).isEqualTo("console-override");
        environment.setProperty("logging.pattern.file", "file-override");
        assertThat(environment.getProperty("logging.pattern.file")).isEqualTo("file-override");
    }

    @Test
    void disabledLoggingDoesNotAddDefaults() {
        MockEnvironment environment = new MockEnvironment().withProperty("loadup.log.enabled", "false");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("logging.pattern.console")).isNull();
    }
}
