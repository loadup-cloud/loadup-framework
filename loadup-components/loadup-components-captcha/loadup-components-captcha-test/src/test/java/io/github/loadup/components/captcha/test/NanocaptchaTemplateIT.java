package io.github.loadup.components.captcha.test;

/*-
 * #%L
 * LoadUp Captcha Test
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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.captcha.CaptchaResponse;
import io.github.loadup.components.captcha.CaptchaTemplate;
import io.github.loadup.components.captcha.CaptchaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test for the nanocaptcha binder.
 */
@SpringBootTest(classes = CaptchaTestApplication.class)
@TestPropertySource(properties = "loadup.captcha.binder-type=nanocaptcha")
class NanocaptchaTemplateIT {

    @Autowired
    private CaptchaTemplate template;

    @Test
    void activeBinder_isNanocaptcha() {
        assertThat(template.getBinderType()).isEqualTo("nanocaptcha");
    }

    @Test
    void generate_returnsWordImage() {
        CaptchaResponse response = template.generate();

        assertThat(response.captchaId()).isNotBlank();
        assertThat(response.type()).isEqualTo(CaptchaType.WORD);
        assertThat(response.backgroundImage()).startsWith("data:image/png;base64,");
        assertThat(response.backgroundImageWidth()).isEqualTo(130);
        assertThat(response.backgroundImageHeight()).isEqualTo(48);
    }

    @Test
    void verify_wrongAnswer_rejects() {
        CaptchaResponse response = template.generate();

        assertThat(template.verify(response.captchaId(), "0000")).isFalse();
    }

    @Test
    void verify_unknownId_rejects() {
        assertThat(template.verify("unknown-id", "1234")).isFalse();
    }
}
