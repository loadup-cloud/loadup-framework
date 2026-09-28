package io.github.loadup.commons.log;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

class LogUtilTest {

    @Test
    void createsLoggerUsingSourceClassName() {
        Logger logger = LogUtil.getLogger(LogUtilTest.class);

        assertThat(logger.getName()).isEqualTo(LogUtilTest.class.getName());
    }

    @Test
    void createsLoggerUsingExplicitName() {
        Logger logger = LogUtil.getLogger("development-diagnostics");

        assertThat(logger.getName()).isEqualTo("development-diagnostics");
    }

    @Test
    void convenienceLoggingMethodsAreCallable() {
        LogUtil.trace("trace message {}", "value");
        LogUtil.debug("debug message {}", "value");
        LogUtil.info("info message {}", "value");
        LogUtil.warn("warn message {}", "value");
        LogUtil.error("error message {}", "value");
    }
}
