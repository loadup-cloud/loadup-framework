package io.github.loadup.components.captcha;

/**
 * Captcha type identifiers accepted by {@link CaptchaTemplate#generate(String)}.
 *
 * <p>This is the union of the captcha families supported by the binders. A binder only supports
 * the types its underlying engine implements; unsupported types are rejected by that binder.
 */
public final class CaptchaType {

    /** Slider (drag) behavior captcha — tianai. */
    public static final String SLIDER = "SLIDER";

    /** Rotate behavior captcha — tianai. */
    public static final String ROTATE = "ROTATE";

    /** Puzzle concat behavior captcha — tianai. */
    public static final String CONCAT = "CONCAT";

    /** Word-image click behavior captcha — tianai. */
    public static final String WORD_IMAGE_CLICK = "WORD_IMAGE_CLICK";

    /** Classic character image captcha — nanocaptcha. */
    public static final String WORD = "WORD";

    private CaptchaType() {
        // Utility class
    }
}
