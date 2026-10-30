/*
 * #%L
 * Loadup UPMS Test
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
package io.github.loadup.modules.upms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.upms.client.validation.PasswordPolicy;
import io.github.loadup.modules.upms.client.validation.PasswordPolicyValidator;
import io.github.loadup.modules.upms.client.validation.ValidPassword;
import org.junit.jupiter.api.Test;

class PasswordPolicyTest {
    @Test
    void enforcesUnicodeCharacterMinimumAndUtf8ByteMaximum() {
        for (String password : java.util.List.of("abcdefgh", "a".repeat(72), "界".repeat(24), "😀".repeat(8))) {
            assertThat(PasswordPolicy.isValid(password)).isTrue();
            assertThat(new PasswordPolicyValidator().isValid(password, null)).isTrue();
        }
        for (String password :
                java.util.List.of("a".repeat(7), "a".repeat(73), "界".repeat(25), "😀".repeat(7), " ".repeat(8))) {
            assertThat(PasswordPolicy.isValid(password)).isFalse();
            assertThatThrownBy(() -> PasswordPolicy.requireValid(password)).isInstanceOf(CommonException.class);
        }
        assertThat(PasswordPolicy.isValid(null)).isFalse();
    }

    @Test
    void passwordEntryFieldsShareOneValidationAnnotation() throws Exception {
        for (String name : java.util.List.of(
                "UserCreateCommand",
                "UserRegisterCommand",
                "UserPasswordChangeCommand",
                "UserPasswordResetCommand",
                "AccountPasswordChangeCommand")) {
            Class<?> type = Class.forName("io.github.loadup.modules.upms.client.command." + name);
            String field =
                    name.equals("UserCreateCommand") || name.equals("UserRegisterCommand") ? "password" : "newPassword";
            assertThat(type.getDeclaredField(field).isAnnotationPresent(ValidPassword.class))
                    .as(name)
                    .isTrue();
        }
    }
}
