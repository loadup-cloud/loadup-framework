package io.github.loadup.modules.upms.client.constant;

/*-
 * #%L
 * Loadup Modules UPMS Client Layer
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
 * 登录类型常量
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public final class LoginType {

    private LoginType() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 账号密码登录
     */
    public static final String PASSWORD = "PASSWORD";

    /**
     * 手机验证码登录
     */
    public static final String MOBILE = "MOBILE";

    /**
     * 邮箱验证码登录
     */
    public static final String EMAIL = "EMAIL";

    /**
     * OAuth 登录
     */
    public static final String OAUTH = "OAUTH";
}
