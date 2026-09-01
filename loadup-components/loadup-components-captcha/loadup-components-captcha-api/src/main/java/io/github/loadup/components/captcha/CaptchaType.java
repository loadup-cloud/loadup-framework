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
