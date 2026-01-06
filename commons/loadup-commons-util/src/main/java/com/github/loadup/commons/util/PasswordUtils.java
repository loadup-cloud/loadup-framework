package com.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.github.loadup.commons.error.AssertUtil;
import java.util.Base64;
import org.apache.commons.lang3.RandomUtils;
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

  /** 盐值长度（字节） */
  private static final int SALT_LENGTH = 8;

  /**
   * 获取加密算法中使用的随机盐值
   *
   * <p>盐长度为8字节，解密时使用的盐值必须与加密时使用的相同
   *
   * @return 8位随机盐值字符串
   */
  public static String getRandomSalt() {
    Long randomLong = RandomUtils.nextLong(10000000L, 99999999L);
    String salt = String.valueOf(randomLong);
    log.debug("Generated random salt: {}", salt);
    return salt;
  }

  /**
   * 加密明文字符串
   *
   * @param plainText 明文字符串
   * @param password 加密密码
   * @param salt 盐值（必须为8位）
   * @return Base64编码的加密字符串
   * @throws IllegalArgumentException 如果参数为空或盐值长度不正确
   */
  public static String encrypt(String plainText, String password, String salt) {
    // 参数验证
    AssertUtil.notEmpty(plainText, "plainText must not be empty");
    AssertUtil.notEmpty(password, "password must not be empty");
    AssertUtil.notEmpty(salt, "salt must not be empty");
    AssertUtil.isTrue(
        StringUtils.length(salt) == SALT_LENGTH, "salt length must be " + SALT_LENGTH);

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
   * @param password 解密密码（必须与加密时使用的密码相同）
   * @param salt 盐值（必须与加密时使用的盐值相同，且为8位）
   * @return 解密后的明文字符串，如果解密失败则返回null
   * @throws IllegalArgumentException 如果参数为空或盐值长度不正确
   */
  public static String decrypt(String encryptedStr, String password, String salt) {
    // 参数验证
    AssertUtil.notEmpty(encryptedStr, "encryptedStr must not be empty");
    AssertUtil.notEmpty(password, "password must not be empty");
    AssertUtil.notEmpty(salt, "salt must not be empty");
    AssertUtil.isTrue(
        StringUtils.length(salt) == SALT_LENGTH, "salt length must be " + SALT_LENGTH);

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
