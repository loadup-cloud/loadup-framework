package io.github.loadup.components.captcha;

/*-
 * #%L
 * loadup-components-captcha
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
 *     <p>验证码配置类
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
