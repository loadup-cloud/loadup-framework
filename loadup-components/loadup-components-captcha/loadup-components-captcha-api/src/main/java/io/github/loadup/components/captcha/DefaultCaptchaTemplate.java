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
