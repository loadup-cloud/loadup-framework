package io.github.loadup.components.captcha.tianai;

/*-
 * #%L
 * LoadUp Captcha Binder Tianai
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
