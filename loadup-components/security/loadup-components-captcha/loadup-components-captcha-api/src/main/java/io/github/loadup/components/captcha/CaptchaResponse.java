package io.github.loadup.components.captcha;

/**
 * Captcha generation result.
 *
 * <p>Images are base64 data URIs. {@code templateImage} (and its tag/width/height) is only set
 * for behavior captchas such as slider / rotate; {@code data} carries binder-specific extras
 * (for example tianai click point definitions).
 *
 * @param captchaId server-side verification key, passed back to {@link CaptchaTemplate#verify}
 * @param type captcha type, one of {@link CaptchaType}
 * @param backgroundImage base64 data URI of the background image
 * @param templateImage base64 data URI of the puzzle template image, or {@code null}
 * @param backgroundImageTag media type of the background image (e.g. {@code image/png})
 * @param templateImageTag media type of the template image, or {@code null}
 * @param backgroundImageWidth width of the background image in pixels
 * @param backgroundImageHeight height of the background image in pixels
 * @param templateImageWidth width of the template image in pixels, or {@code null}
 * @param templateImageHeight height of the template image in pixels, or {@code null}
 * @param data binder-specific extra payload, or {@code null}
 */
public record CaptchaResponse(
        String captchaId,
        String type,
        String backgroundImage,
        String templateImage,
        String backgroundImageTag,
        String templateImageTag,
        Integer backgroundImageWidth,
        Integer backgroundImageHeight,
        Integer templateImageWidth,
        Integer templateImageHeight,
        Object data) {}
