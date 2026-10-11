/*
 * #%L
 * Loadup Common Utils
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
package io.github.loadup.commons.util;

import io.github.loadup.commons.log.LogUtil;
import java.security.SecureRandom;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.encrypt.Encryptors;

/**
 *
 *
 * <ul>
 * </ul>
 *
 *
 * @author loadup_cloud
 * @since 1.0.0
 */
public class PasswordUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int SALT_LENGTH = 8;

    public static String getRandomSalt() {
        Long randomLong = SECURE_RANDOM.nextLong(10000000L, 100000000L);
        String salt = String.valueOf(randomLong);
        LogUtil.debug(PasswordUtils.class, "Generated random salt: {}", salt);
        return salt;
    }

    public static String encrypt(String plainText, String password, String salt) {

        AssertUtil.notEmpty(plainText, "plainText must not be empty");
        AssertUtil.notEmpty(password, "password must not be empty");
        AssertUtil.notEmpty(salt, "salt must not be empty");
        AssertUtil.isTrue(StringUtils.length(salt) == SALT_LENGTH, "salt length must be " + SALT_LENGTH);

        try {
            byte[] encryptedByte = Encryptors.stronger(password, salt).encrypt(plainText.getBytes());
            String encrypted = Base64.getEncoder().encodeToString(encryptedByte);
            LogUtil.debug(PasswordUtils.class, "Successfully encrypted text with length: {}", plainText.length());
            return encrypted;
        } catch (Exception e) {
            LogUtil.error(PasswordUtils.class, "Failed to encrypt text", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public static String decrypt(String encryptedStr, String password, String salt) {

        AssertUtil.notEmpty(encryptedStr, "encryptedStr must not be empty");
        AssertUtil.notEmpty(password, "password must not be empty");
        AssertUtil.notEmpty(salt, "salt must not be empty");
        AssertUtil.isTrue(StringUtils.length(salt) == SALT_LENGTH, "salt length must be " + SALT_LENGTH);

        try {
            byte[] decodedByte = Base64.getDecoder().decode(encryptedStr);
            byte[] decrypted = Encryptors.stronger(password, salt).decrypt(decodedByte);
            String decryptedText = new String(decrypted);
            LogUtil.debug(PasswordUtils.class, "Successfully decrypted text with length: {}", decryptedText.length());
            return decryptedText;
        } catch (Exception e) {
            LogUtil.error(PasswordUtils.class, "Failed to decrypt text: {}", e.getMessage());
            return null;
        }
    }

    public static boolean isValidSalt(String salt) {
        return salt != null && salt.length() == SALT_LENGTH;
    }
}
