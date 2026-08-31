package io.github.loadup.components.captcha.utils;

/*-
 * #%L
 * loadup-components-captcha
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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
 * 图形验证码工具抽象父类
 *
 * @author FULaBUla
 * @since 2023-01-28 上午 10:08
 */
public abstract class AbstractCaptchaUtil {

    /**
     * session 键名
     */
    protected static final String SESSION_KEY = "captcha";

    /**
     * 默认长度
     */
    protected static final int DEFAULT_LEN = 4;

    /**
     * 默认宽度
     */
    protected static final int DEFAULT_WIDTH = 130;

    /**
     * 默认高度
     */
    protected static final int DEFAULT_HEIGHT = 48;
}
