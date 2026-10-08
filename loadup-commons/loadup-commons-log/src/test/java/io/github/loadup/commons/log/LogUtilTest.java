package io.github.loadup.commons.log;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

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
    @ExtendWith(OutputCaptureExtension.class)
    void retainsParameterizedMessagesAndTrailingException(CapturedOutput output) {
        LogUtil.error(LogUtilTest.class, "Failed order id={}", "order-1", new IllegalStateException("test-failure"));
        assertThat(output.getOut())
                .contains(
                        LogUtilTest.class.getSimpleName(),
                        "Failed order id=order-1",
                        "IllegalStateException: test-failure");
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
