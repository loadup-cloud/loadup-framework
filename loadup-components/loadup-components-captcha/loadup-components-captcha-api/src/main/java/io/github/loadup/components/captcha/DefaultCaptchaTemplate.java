package io.github.loadup.components.captcha;

/** Default {@link CaptchaTemplate} that delegates to the single active {@link CaptchaProvider}. */
public class DefaultCaptchaTemplate implements CaptchaTemplate {

    private final CaptchaProvider provider;

    public DefaultCaptchaTemplate(CaptchaProvider provider) {
        this.provider = provider;
    }

    @Override
    public CaptchaResponse generate() {
        return provider.generate(null);
    }

    @Override
    public CaptchaResponse generate(String type) {
        return provider.generate(type);
    }

    @Override
    public boolean verify(String captchaId, Object userInput) {
        return provider.verify(captchaId, userInput);
    }

    @Override
    public String getBinderType() {
        return provider.getBinderType();
    }
}
