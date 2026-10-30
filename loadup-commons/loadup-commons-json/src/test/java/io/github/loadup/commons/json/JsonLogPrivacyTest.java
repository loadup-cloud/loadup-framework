/*
 * #%L
 * LoadUp Common JSON
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
package io.github.loadup.commons.json;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.loadup.commons.log.LogUtil;
import org.junit.jupiter.api.Test;

class JsonLogPrivacyTest {
    @Test
    void parsingErrorsNeverLogPayloadOrJacksonExceptionText() {
        var logger = (Logger) LogUtil.getLogger(JsonUtil.class);
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        try {
            assertThat(JsonUtil.fromJson("{\"password\":\"private-secret", Object.class))
                    .isNull();
            assertThat(appender.list).isNotEmpty();
            for (var event : appender.list) {
                assertThat(event.getFormattedMessage()).doesNotContain("private-secret", "password");
                assertThat(event.getThrowableProxy()).isNull();
            }
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
