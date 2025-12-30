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
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.encrypt.Encryptors;

import java.util.Base64;

/**
 * @Description: 密码工具类
 */
public class PasswordUtils {
    // 实例化安全随机数
    private static final int SALT_LENGTH = 8;

    /**
     * 获取加密算法中使用的盐值,解密中使用的盐值必须与加密中使用的相同才能完成操作. 盐长度必须为8字节
     */
    public static String getRandomSalt() {
        // 产出盐
        Long randomLong = RandomUtils.nextLong(10000000L, 99999999L);
        return new String(String.valueOf(randomLong));
    }

    public static String encrypt(String plainText, String password, String salt) {
        AssertUtil.notEmpty(plainText, "plainText must not be empty");
        AssertUtil.notEmpty(password, "password must not be empty");
        AssertUtil.notEmpty(salt, "salt must not be empty");
        AssertUtil.isTrue(StringUtils.length(salt) == SALT_LENGTH, "salt length must be " + SALT_LENGTH);
        byte[] encryptedByte = Encryptors.stronger(password, salt).encrypt(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedByte);
    }

    public static String decrypt(String encryptedStr, String password, String salt) {
        AssertUtil.notEmpty(encryptedStr, "encryptStr must not be empty");
        AssertUtil.notEmpty(password, "password must not be empty");
        AssertUtil.notEmpty(salt, "salt must not be empty");
        AssertUtil.isTrue(StringUtils.length(salt) == SALT_LENGTH, "salt length must be " + SALT_LENGTH);
        try {
            byte[] decodedByte = Base64.getDecoder().decode(encryptedStr);
            byte[] decrypt = Encryptors.stronger(password, salt).decrypt(decodedByte);
            return new String(decrypt);
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        String randomSalt = getRandomSalt();
        System.out.println(randomSalt);
        String encrypt = PasswordUtils.encrypt("123456", "123456", randomSalt);
        System.out.println(encrypt);
        String decrypt =
            PasswordUtils.decrypt("ay+RdxDHbBn/AW/bD80IhHJq6YKnFKNNfgXMCfNdjPf1v6/iwPQ=", "123456", "69496770");
        System.out.println(decrypt);
    }
}
