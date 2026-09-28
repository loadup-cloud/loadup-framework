package io.github.loadup.components.captcha;

/**
 * SPI implemented by each captcha binder.
 *
 * <p>A binder adapts one underlying captcha engine (tianai behavior captcha, nanocaptcha image
 * captcha, ...) to the LoadUp facade. Exactly one provider is active per application.
 */
public interface CaptchaProvider {

    /**
     * Generate a captcha of the given type.
     *
     * @param type captcha type from {@link CaptchaType}; {@code null} means the binder default
     * @return generation result
     */
    CaptchaResponse generate(String type);

    /**
     * Verify a previously generated captcha.
     *
     * @param captchaId id returned by {@link #generate(String)}
     * @param userInput binder-specific input: {@link String} answer / JSON track for tianai,
     *     {@link Number} slider percentage, or the engine's own track object
     * @return {@code true} when verification succeeds
     */
    boolean verify(String captchaId, Object userInput);

    /** Binder type, one of {@code tianai} / {@code nanocaptcha}. */
    String getBinderType();
}
