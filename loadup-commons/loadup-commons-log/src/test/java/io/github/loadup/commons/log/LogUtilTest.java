/*-
 * #%L
 * Loadup Common Log
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
