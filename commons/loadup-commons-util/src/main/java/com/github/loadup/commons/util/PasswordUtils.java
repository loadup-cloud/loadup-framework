/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.commons.error.AssertUtil;
import java.util.Base64;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.encrypt.Encryptors;

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
