package io.github.loadup.components.captcha.nanocaptcha.autoconfig;

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
