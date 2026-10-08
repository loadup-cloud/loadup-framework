package io.github.loadup.components.captcha.autoconfig;

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
