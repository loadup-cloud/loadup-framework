/*
 * #%L
 * LoadUp Captcha Binder Nanocaptcha
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
package io.github.loadup.components.captcha.nanocaptcha;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.loadup.components.captcha.CaptchaType;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import net.logicsquad.nanocaptcha.image.ImageCaptcha;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedConstruction;

class NanocaptchaProviderTest {
    @Test
    void successfulAndFailedAttemptsAreBothSingleUse() {
        var properties = new NanocaptchaProperties();
        var provider = new NanocaptchaProvider(properties);
        try (var ignored = images(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB))) {
            var correct = provider.generate(null);
            assertThat(provider.verify(correct.captchaId(), "  aB12  ")).isTrue();
            assertThat(provider.verify(correct.captchaId(), "Ab12")).isFalse();
            var wrong = provider.generate(CaptchaType.WORD);
            assertThat(provider.verify(wrong.captchaId(), "wrong")).isFalse();
            assertThat(provider.verify(wrong.captchaId(), "Ab12")).isFalse();
            assertThat(provider.verify("unknown", "Ab12")).isFalse();
            var invalid = provider.generate(null);
            assertThat(provider.verify(invalid.captchaId(), 12)).isFalse();
            assertThat(provider.verify(invalid.captchaId(), null)).isFalse();
            assertThat(provider.verify(null, "Ab12")).isFalse();
            assertThat(provider.verify(invalid.captchaId(), "Ab12")).isTrue();
            properties.setExpirationSeconds(-1);
            assertThat(provider.verify(provider.generate(null).captchaId(), "Ab12"))
                    .isFalse();
        }
        assertThat(provider.getBinderType()).isEqualTo("nanocaptcha");
        assertThatThrownBy(() -> provider.generate(CaptchaType.SLIDER)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rendersActualPngForEachConfiguredCharacterSet() throws Exception {
        for (String charset : new String[] {"numbers", "latin"}) {
            var properties = new NanocaptchaProperties();
            properties.setContent(charset);
            properties.setWidth(180);
            properties.setHeight(60);
            properties.setLength(5);
            var response = new NanocaptchaProvider(properties).generate(CaptchaType.WORD);
            assertThat(response.type()).isEqualTo(CaptchaType.WORD);
            assertThat(response.backgroundImageTag()).isEqualTo("image/png");
            byte[] png = Base64.getDecoder().decode(response.backgroundImage().split(",", 2)[1]);
            var image = ImageIO.read(new ByteArrayInputStream(png));
            assertThat(image.getWidth()).isEqualTo(180);
            assertThat(image.getHeight()).isEqualTo(60);
            assertThat(response.backgroundImageWidth()).isEqualTo(image.getWidth());
            assertThat(response.backgroundImageHeight()).isEqualTo(image.getHeight());
        }
    }

    @Test
    void expiredEntriesArePurgedWithoutDiscardingFreshChallenges() {
        var properties = new NanocaptchaProperties();
        var provider = new NanocaptchaProvider(properties);
        try (var ignored = images(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB))) {
            properties.setExpirationSeconds(-1);
            String expired = provider.generate(null).captchaId();
            for (int i = 1; i < 2500; i++) provider.generate(null);
            properties.setExpirationSeconds(300);
            for (int i = 0; i < 2499; i++) provider.generate(null);
            String fresh = provider.generate(null).captchaId();
            assertThat(provider.verify(expired, "Ab12")).isFalse();
            assertThat(provider.verify(fresh, "Ab12")).isTrue();
        }
    }

    @Test
    void imageEncodingFailureIsReported() {
        try (var ignored = images(null)) {
            assertThatThrownBy(() -> new NanocaptchaProvider(new NanocaptchaProperties()).generate(null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Failed to encode captcha image");
        }
    }

    @Test
    void rejectsRemovedChineseAndUnknownCharacterSets() {
        for (String charset : new String[] {"chinese", "unknown"}) {
            var properties = new NanocaptchaProperties();
            properties.setContent(charset);
            assertThatThrownBy(() -> new NanocaptchaProvider(properties).generate(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Unsupported nanocaptcha content: " + charset);
        }
    }

    private static MockedConstruction<ImageCaptcha.Factory.Builder> images(BufferedImage image) {
        ImageCaptcha captcha = mock(ImageCaptcha.class);
        when(captcha.getContent()).thenReturn("Ab12");
        when(captcha.getImage()).thenReturn(image);
        ImageCaptcha.Factory factory = mock(ImageCaptcha.Factory.class);
        when(factory.create()).thenReturn(captcha);
        return mockConstruction(
                ImageCaptcha.Factory.Builder.class,
                withSettings().defaultAnswer(Answers.RETURNS_SELF),
                (builder, context) -> when(builder.build()).thenReturn(factory));
    }
}
