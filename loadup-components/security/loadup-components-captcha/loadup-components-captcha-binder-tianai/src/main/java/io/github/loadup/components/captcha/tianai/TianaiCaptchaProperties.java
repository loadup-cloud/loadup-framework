package io.github.loadup.components.captcha.tianai;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tianai binder configuration ({@code loadup.captcha.binder.tianai.*}).
 */
@ConfigurationProperties(prefix = "loadup.captcha.binder.tianai")
public class TianaiCaptchaProperties {

    /** Whether to load the default slider / rotate templates bundled in the tianai jar. */
    private boolean initDefaultResource = true;

    /** Default captcha type used by {@code generate()}, one of the {@code CaptchaType} constants. */
    private String defaultType = "SLIDER";

    /** Verification expiration in seconds, keyed by captcha type; {@code default} applies to all. */
    private Map<String, Long> expireSeconds = new LinkedHashMap<>();

    public boolean isInitDefaultResource() {
        return initDefaultResource;
    }

    public void setInitDefaultResource(boolean initDefaultResource) {
        this.initDefaultResource = initDefaultResource;
    }

    public String getDefaultType() {
        return defaultType;
    }

    public void setDefaultType(String defaultType) {
        this.defaultType = defaultType;
    }

    public Map<String, Long> getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(Map<String, Long> expireSeconds) {
        this.expireSeconds = expireSeconds;
    }
}
