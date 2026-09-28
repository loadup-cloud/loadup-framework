package io.github.loadup.components.captcha.nanocaptcha;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.captcha.CaptchaResponse;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NanocaptchaProviderTest {

    @Test
    void verify_correctAnswer_succeedsOnce() throws Exception {
        NanocaptchaProvider provider = new NanocaptchaProvider(new NanocaptchaProperties());
        CaptchaResponse response = provider.generate(null);

        String answer = answerOf(provider, response.captchaId());

        assertThat(answer).isNotBlank();
        assertThat(provider.verify(response.captchaId(), answer)).isTrue();
        assertThat(provider.verify(response.captchaId(), answer)).isFalse();
    }

    @Test
    void verify_ignoresCaseAndWhitespace() throws Exception {
        NanocaptchaProvider provider = new NanocaptchaProvider(new NanocaptchaProperties());
        CaptchaResponse response = provider.generate(null);

        String answer = answerOf(provider, response.captchaId());

        assertThat(provider.verify(response.captchaId(), "  " + answer.toUpperCase() + " "))
                .isTrue();
    }

    @Test
    void verify_wrongAnswer_rejects() throws Exception {
        NanocaptchaProvider provider = new NanocaptchaProvider(new NanocaptchaProperties());
        CaptchaResponse response = provider.generate(null);

        assertThat(provider.verify(response.captchaId(), "wrong")).isFalse();
    }

    @Test
    void generate_unsupportedType_rejects() {
        NanocaptchaProvider provider = new NanocaptchaProvider(new NanocaptchaProperties());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> provider.generate("SLIDER"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static String answerOf(NanocaptchaProvider provider, String captchaId) throws Exception {
        Field pendingField = NanocaptchaProvider.class.getDeclaredField("pending");
        pendingField.setAccessible(true);
        Map<?, ?> pending = (Map<?, ?>) pendingField.get(provider);
        Object entry = pending.get(captchaId);
        Field answerField = entry.getClass().getDeclaredField("answer");
        answerField.setAccessible(true);
        return (String) answerField.get(entry);
    }
}
