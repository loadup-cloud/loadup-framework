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
