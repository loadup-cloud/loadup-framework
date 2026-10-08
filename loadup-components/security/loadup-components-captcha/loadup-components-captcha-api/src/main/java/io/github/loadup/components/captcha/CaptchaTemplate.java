package io.github.loadup.components.captcha;

/**
 * Business-facing captcha facade.
 *
 * <p>Business code injects this interface and never touches the underlying engine. Switching the
 * backend means changing the binder dependency and {@code loadup.captcha.binder-type} only.
 */
public interface CaptchaTemplate {

    /** Generate a captcha of the binder's default type. */
    CaptchaResponse generate();

    /** Generate a captcha of the requested {@link CaptchaType}. */
    CaptchaResponse generate(String type);

    /** Verify a previously generated captcha; see {@link CaptchaProvider#verify}. */
    boolean verify(String captchaId, Object userInput);

    /** Active binder type, one of {@code tianai} / {@code nanocaptcha}. */
    String getBinderType();
}
