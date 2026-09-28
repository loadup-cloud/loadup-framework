package io.github.loadup.components.captcha.test;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.captcha.CaptchaResponse;
import io.github.loadup.components.captcha.CaptchaTemplate;
import io.github.loadup.components.captcha.CaptchaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test for the tianai binder (default).
 *
 * <p>Verifies generation through the facade and that wrong / unknown inputs are rejected. The
 * correct slider answer is never exposed to clients, so the success path is verified at the
 * engine level by tianai's own test suite.
 */
@SpringBootTest(classes = CaptchaTestApplication.class)
class TianaiCaptchaTemplateIT {

    @Autowired
    private CaptchaTemplate template;

    @Test
    void activeBinder_isTianai() {
        assertThat(template.getBinderType()).isEqualTo("tianai");
    }

    @Test
    void generate_defaultType_returnsSliderPayload() {
        CaptchaResponse response = template.generate();

        assertThat(response.captchaId()).isNotBlank();
        assertThat(response.type()).isEqualTo(CaptchaType.SLIDER);
        assertThat(response.backgroundImage()).startsWith("data:image/");
        assertThat(response.templateImage()).startsWith("data:image/");
        assertThat(response.backgroundImageWidth()).isPositive();
        assertThat(response.backgroundImageHeight()).isPositive();
    }

    @Test
    void generate_rotateType_returnsRotatePayload() {
        CaptchaResponse response = template.generate(CaptchaType.ROTATE);

        assertThat(response.captchaId()).isNotBlank();
        assertThat(response.type()).isEqualTo(CaptchaType.ROTATE);
        assertThat(response.templateImage()).startsWith("data:image/");
    }

    @Test
    void verify_wrongPercentage_rejects() {
        CaptchaResponse response = template.generate();

        assertThat(template.verify(response.captchaId(), 0.5f)).isFalse();
    }

    @Test
    void verify_unknownId_rejects() {
        assertThat(template.verify("unknown-id", 0.5f)).isFalse();
    }
}
