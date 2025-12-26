package com.github.loadup.components.captcha;

import com.github.loadup.components.captcha.base.Randoms;
import com.github.loadup.components.captcha.config.CaptchaEndpoint;
import com.github.loadup.components.captcha.config.CaptchaProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author lengleng
 * @date 2020/7/31
 * <p>
 * 验证码配置类
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
        if (properties.getCustomCharacters() != null && !properties.getCustomCharacters().isEmpty()) {
            Randoms.setCustomAlpha(properties.getCustomCharacters());
        }
    }

    @Bean
    @ConditionalOnWebApplication
    public CaptchaEndpoint captchaEndpoint(CaptchaProperties properties) {
        return new CaptchaEndpoint(properties);
    }

}
