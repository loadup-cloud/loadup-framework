/*
 * #%L
 * Loadup Modules UPMS App Layer
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
package io.github.loadup.modules.upms.app.strategy;

/**
 * OAuth 提供商常量
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public final class OAuthProviderCode {

    private OAuthProviderCode() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * GitHub
     */
    public static final String GITHUB = "github";

    /**
     * 微信
     */
    public static final String WECHAT = "wechat";

    /**
     * Google
     */
    public static final String GOOGLE = "google";
}
