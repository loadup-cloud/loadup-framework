package io.github.loadup.components.captcha;

/*-
 * #%L
 * LoadUp Captcha Components API
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
