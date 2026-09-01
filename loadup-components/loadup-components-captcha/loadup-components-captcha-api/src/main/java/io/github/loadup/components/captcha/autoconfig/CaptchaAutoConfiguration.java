package io.github.loadup.components.captcha.autoconfig;

/*-
 * #%L
 * LoadUp Captcha Components API
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

import io.github.loadup.components.captcha.CaptchaProperties;
import io.github.loadup.components.captcha.CaptchaProvider;
import io.github.loadup.components.captcha.CaptchaTemplate;
import io.github.loadup.components.captcha.DefaultCaptchaTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Creates the {@link CaptchaTemplate} from the single active {@link CaptchaProvider}. */
@AutoConfiguration
@ConditionalOnSingleCandidate(CaptchaProvider.class)
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CaptchaTemplate.class)
    public CaptchaTemplate captchaTemplate(CaptchaProvider provider) {
        return new DefaultCaptchaTemplate(provider);
    }
}
