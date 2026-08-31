package io.github.loadup.components.captcha;

/*-
 * #%L
 * loadup-components-captcha
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import io.github.loadup.components.captcha.base.Randoms;
import io.github.loadup.components.captcha.config.CaptchaEndpoint;
import io.github.loadup.components.captcha.config.CaptchaProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lengleng
 * @date 2020/7/31
 * <p>验证码配置类
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaAutoConfiguration {

    private final CaptchaProperties properties;

    public CaptchaAutoConfiguration(CaptchaProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        // 如果配置了自定义字符集，则应用
        if (properties.getCustomCharacters() != null
                && !properties.getCustomCharacters().isEmpty()) {
            Randoms.setCustomAlpha(properties.getCustomCharacters());
        }
    }

    @Bean
    @ConditionalOnWebApplication
    public CaptchaEndpoint captchaEndpoint(CaptchaProperties properties) {
        return new CaptchaEndpoint(properties);
    }
}
