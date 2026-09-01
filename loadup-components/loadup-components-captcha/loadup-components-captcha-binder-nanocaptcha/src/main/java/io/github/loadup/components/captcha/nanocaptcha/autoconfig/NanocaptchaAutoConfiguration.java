package io.github.loadup.components.captcha.nanocaptcha.autoconfig;

/*-
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

import io.github.loadup.components.captcha.CaptchaProvider;
import io.github.loadup.components.captcha.autoconfig.CaptchaAutoConfiguration;
import io.github.loadup.components.captcha.nanocaptcha.NanocaptchaProperties;
import io.github.loadup.components.captcha.nanocaptcha.NanocaptchaProvider;
import net.logicsquad.nanocaptcha.image.ImageCaptcha;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the nanocaptcha image captcha binder.
 */
@AutoConfiguration(before = CaptchaAutoConfiguration.class)
@ConditionalOnClass(ImageCaptcha.class)
@ConditionalOnProperty(prefix = "loadup.captcha", name = "binder-type", havingValue = "nanocaptcha")
@EnableConfigurationProperties(NanocaptchaProperties.class)
public class NanocaptchaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CaptchaProvider nanocaptchaProvider(NanocaptchaProperties properties) {
        return new NanocaptchaProvider(properties);
    }
}
