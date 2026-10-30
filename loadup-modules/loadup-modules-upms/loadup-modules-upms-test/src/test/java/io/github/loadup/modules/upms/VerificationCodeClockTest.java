/*
 * #%L
 * Loadup UPMS Test
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
package io.github.loadup.modules.upms;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.modules.upms.app.service.VerificationCodeService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class VerificationCodeClockTest {
    @Test
    void enforcesResendAndExpiryWithoutLeakingCodesOrRecipients() {
        var clock = new MutableClock();
        var service = new VerificationCodeService(clock);
        var logger = (Logger) LogUtil.getLogger(VerificationCodeService.class);
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        try {
            String email = "private.person@example.test";
            String code = service.generateEmailCode(email);
            assertThat(service.canSendCode(email, "EMAIL")).isFalse();
            clock.now = clock.now.plusSeconds(60);
            assertThat(service.canSendCode(email, "EMAIL")).isTrue();
            clock.now = clock.now.plusSeconds(240);
            assertThat(service.verifyEmailCode(email, code)).isFalse();
            String sms = service.generateSmsCode("13812345678");
            assertThat(service.verifySmsCode("13812345678", sms)).isTrue();
            assertThat(service.verifySmsCode("13812345678", sms)).isFalse();
            for (var event : appender.list) {
                assertThat(event.getFormattedMessage()).doesNotContain(code, sms, email, "13812345678");
                assertThat(event.getThrowableProxy()).isNull();
            }
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    static class MutableClock extends Clock {
        Instant now = Instant.parse("2026-10-11T00:00:00Z");

        public ZoneId getZone() {
            return ZoneId.of("Asia/Shanghai");
        }

        public Clock withZone(ZoneId zone) {
            return this;
        }

        public Instant instant() {
            return now;
        }
    }
}
