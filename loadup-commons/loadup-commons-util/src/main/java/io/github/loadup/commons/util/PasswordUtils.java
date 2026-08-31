package io.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import java.security.SecureRandom;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.encrypt.Encryptors;

/**
 * 密码工具类
 *
 * <p>提供密码加密、解密和盐值生成功能：
 *
 * <ul>
 *   <li>随机盐值生成
 *   <li>字符串加密（AES-256）
 *   <li>字符串解密
 *   <li>完整的参数验证
 * </ul>
 *
 * <p>加密算法使用 Spring Security 的 Encryptors.stronger()，提供 AES-256 加密。
 *
 * @author loadup_cloud
 * @since 1.0.0
 */
public class PasswordUtils {

    private static final Logger log = LoggerFactory.getLogger(PasswordUtils.class);

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 盐值长度（字节）
     */
    private static final int SALT_LENGTH = 8;

    /**
     * 获取加密算法中使用的随机盐值
     *
     * <p>盐长度为8字节，解密时使用的盐值必须与加密时使用的相同
     *
     * @return 8位随机盐值字符串
     */
    public static String getRandomSalt() {
        Long randomLong = SECURE_RANDOM.nextLong(10000000L, 100000000L);
        String salt = String.valueOf(randomLong);
        log.debug("Generated random salt: {}", salt);
        return salt;
    }

    /**
     * 加密明文字符串
     *
     * @param plainText 明文字符串
     * @param password  加密密码
     * @param salt      盐值（必须为8位）
     * @return Base64编码的加密字符串
     * @throws IllegalArgumentException 如果参数为空或盐值长度不正确
     */
    public static String encrypt(String plainText, String password, String salt) {
        // 参数验证
        AssertUtil.notEmpty(plainText, "plainText must not be empty");
        AssertUtil.notEmpty(password, "password must not be empty");
        AssertUtil.notEmpty(salt, "salt must not be empty");
        AssertUtil.isTrue(StringUtils.length(salt) == SALT_LENGTH, "salt length must be " + SALT_LENGTH);

        try {
            byte[] encryptedByte = Encryptors.stronger(password, salt).encrypt(plainText.getBytes());
            String encrypted = Base64.getEncoder().encodeToString(encryptedByte);
            log.debug("Successfully encrypted text with length: {}", plainText.length());
            return encrypted;
        } catch (Exception e) {
            log.error("Failed to encrypt text", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * 解密加密字符串
     *
     * @param encryptedStr Base64编码的加密字符串
     * @param password     解密密码（必须与加密时使用的密码相同）
     * @param salt         盐值（必须与加密时使用的盐值相同，且为8位）
     * @return 解密后的明文字符串，如果解密失败则返回null
     * @throws IllegalArgumentException 如果参数为空或盐值长度不正确
     */
    public static String decrypt(String encryptedStr, String password, String salt) {
        // 参数验证
        AssertUtil.notEmpty(encryptedStr, "encryptedStr must not be empty");
        AssertUtil.notEmpty(password, "password must not be empty");
        AssertUtil.notEmpty(salt, "salt must not be empty");
        AssertUtil.isTrue(StringUtils.length(salt) == SALT_LENGTH, "salt length must be " + SALT_LENGTH);

        try {
            byte[] decodedByte = Base64.getDecoder().decode(encryptedStr);
            byte[] decrypted = Encryptors.stronger(password, salt).decrypt(decodedByte);
            String decryptedText = new String(decrypted);
            log.debug("Successfully decrypted text with length: {}", decryptedText.length());
            return decryptedText;
        } catch (Exception e) {
            log.error("Failed to decrypt text: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证盐值是否有效
     *
     * @param salt 盐值
     * @return true-有效，false-无效
     */
    public static boolean isValidSalt(String salt) {
        return salt != null && salt.length() == SALT_LENGTH;
    }
}
