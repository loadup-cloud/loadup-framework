/*
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
package io.github.loadup.components.captcha.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import io.github.loadup.components.captcha.CaptchaProvider;
import io.github.loadup.components.captcha.tianai.TianaiCaptchaProperties;
import io.github.loadup.components.captcha.tianai.TianaiCaptchaProvider;
import io.github.loadup.components.captcha.tianai.autoconfigure.TianaiCaptchaAutoConfiguration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class TianaiCaptchaBoundaryTest {
    @Test
    void generationHonorsDefaultAndExplicitTypeAndPreservesPayload() {
        var application = mock(ImageCaptchaApplication.class);
        var provider = new TianaiCaptchaProvider(application, "ROTATE");
        var vo = new ImageCaptchaVO(
                "id",
                "ROTATE",
                "background",
                "template",
                "image/png",
                "image/png",
                320,
                180,
                60,
                60,
                Map.of("public", "metadata"));
        when(application.generateCaptcha("ROTATE")).thenReturn(ApiResponse.ofSuccess(vo));
        var response = provider.generate(null);
        assertThat(response.captchaId()).isEqualTo("id");
        assertThat(response.type()).isEqualTo("ROTATE");
        assertThat(response.backgroundImage()).isEqualTo("background");
        assertThat(response.templateImage()).isEqualTo("template");
        assertThat(response.backgroundImageWidth()).isEqualTo(320);
        assertThat(response.backgroundImageHeight()).isEqualTo(180);
        assertThat(response.templateImageWidth()).isEqualTo(60);
        assertThat(response.templateImageHeight()).isEqualTo(60);
        assertThat(response.backgroundImageTag()).isEqualTo("image/png");
        assertThat(response.templateImageTag()).isEqualTo("image/png");
        assertThat(response.data()).isEqualTo(vo.getData());
        when(application.generateCaptcha("SLIDER")).thenReturn(ApiResponse.ofError("unavailable"));
        assertThatThrownBy(() -> provider.generate("SLIDER")).isInstanceOf(IllegalStateException.class);
        when(application.generateCaptcha("SLIDER")).thenReturn(ApiResponse.ofSuccess());
        assertThatThrownBy(() -> provider.generate("SLIDER")).isInstanceOf(IllegalStateException.class);
        assertThat(provider.getBinderType()).isEqualTo("tianai");
    }

    @Test
    void verifiesNumbersTracksAndJsonThroughSdk() {
        var application = mock(ImageCaptchaApplication.class);
        var provider = new TianaiCaptchaProvider(application, "SLIDER");
        when(application.matching("id", 0.25f)).thenReturn(true);
        assertThat(provider.verify("id", 0.25)).isTrue();
        assertThat(provider.verify("id", " 0.25 ")).isTrue();
        assertThat(provider.verify("id", 0.5f)).isFalse();
        doReturn(ApiResponse.ofSuccess()).when(application).matching(eq("id"), any(ImageCaptchaTrack.class));
        var track = new ImageCaptchaTrack();
        assertThat(provider.verify("id", track)).isTrue();
        assertThat(provider.verify("id", "{\"bgImageWidth\":320,\"startTime\":123}"))
                .isTrue();
        var captor = ArgumentCaptor.forClass(ImageCaptchaTrack.class);
        verify(application, times(2)).matching(eq("id"), captor.capture());
        assertThat(captor.getAllValues().getLast().getBgImageWidth()).isEqualTo(320);
        assertThat(captor.getAllValues().getLast().getStartTime()).isEqualTo(123L);
        doReturn(ApiResponse.ofError("invalid")).when(application).matching(eq("id"), any(ImageCaptchaTrack.class));
        assertThat(provider.verify("id", track)).isFalse();
    }

    @Test
    void malformedAndUnsupportedInputsAreRejected() {
        var application = mock(ImageCaptchaApplication.class);
        var provider = new TianaiCaptchaProvider(application, "SLIDER");
        assertThat(provider.verify(null, "0.5")).isFalse();
        assertThat(provider.verify("id", null)).isFalse();
        assertThat(provider.verify("id", new Object())).isFalse();
        assertThat(provider.verify("id", "{malformed-synthetic-payload")).isFalse();
        when(application.matching("id", 0.5f)).thenThrow(new IllegalStateException("synthetic SDK failure"));
        assertThat(provider.verify("id", 0.5f)).isFalse();
    }

    @Test
    void customResourcesAndExpiryConfigurationCanReplaceDefaults() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(TianaiCaptchaAutoConfiguration.class))
                .withPropertyValues(
                        "loadup.captcha.binder.tianai.init-default-resource=false",
                        "loadup.captcha.binder.tianai.default-type=ROTATE",
                        "loadup.captcha.binder.tianai.expire-seconds.default=45")
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(CaptchaProvider.class);
                    var properties = context.getBean(TianaiCaptchaProperties.class);
                    assertThat(properties.isInitDefaultResource()).isFalse();
                    assertThat(properties.getDefaultType()).isEqualTo("ROTATE");
                    assertThat(properties.getExpireSeconds()).containsEntry("default", 45L);
                    properties.setExpireSeconds(Map.of("SLIDER", 30L));
                    assertThat(properties.getExpireSeconds()).containsOnlyKeys("SLIDER");
                });
    }
}
