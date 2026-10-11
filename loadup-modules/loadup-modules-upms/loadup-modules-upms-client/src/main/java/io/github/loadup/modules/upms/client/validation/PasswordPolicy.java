/*
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
package io.github.loadup.modules.upms.client.validation;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import java.nio.charset.StandardCharsets;

/** Shared input policy compatible with the configured BCrypt encoder. */
public final class PasswordPolicy {
    public static final int MIN_CHARACTERS = 8;
    public static final int MAX_UTF8_BYTES = 72;
    public static final String MESSAGE = "Password requires at least 8 Unicode characters and at most 72 UTF-8 bytes";

    private PasswordPolicy() {}

    public static boolean isValid(String value) {
        return value != null
                && !value.isBlank()
                && value.codePointCount(0, value.length()) >= MIN_CHARACTERS
                && value.getBytes(StandardCharsets.UTF_8).length <= MAX_UTF8_BYTES;
    }

    public static void requireValid(String value) {
        if (!isValid(value)) throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL, MESSAGE);
    }
}
