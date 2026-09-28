package io.github.loadup.components.captcha;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LoadUp captcha facade configuration ({@code loadup.captcha.*}).
 */
@ConfigurationProperties(prefix = "loadup.captcha")
public class CaptchaProperties {

    /** Binder selector, one of {@code tianai} (default) / {@code nanocaptcha}. */
    private String binderType = "tianai";

    public String getBinderType() {
        return binderType;
    }

    public void setBinderType(String binderType) {
        this.binderType = binderType;
    }
}
