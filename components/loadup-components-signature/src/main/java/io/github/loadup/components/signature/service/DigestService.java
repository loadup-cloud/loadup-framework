package io.github.loadup.components.signature.service;

/*-
 * #%L
 * LoadUp Components :: Signature
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import io.github.loadup.components.signature.enums.DigestAlgorithm;

/**
 * 摘要服务接口
 *
 * @author loadup
 */
public interface DigestService {

    /**
     * 计算数据的摘要
     *
     * @param data      待计算数据
     * @param algorithm 摘要算法
     * @return Hex 编码的摘要结果
     */
    String digest(byte[] data, DigestAlgorithm algorithm);

    /**
     * 计算字符串的摘要
     *
     * @param data      待计算字符串
     * @param algorithm 摘要算法
     * @return Hex 编码的摘要结果
     */
    String digest(String data, DigestAlgorithm algorithm);

    /**
     * 计算 HMAC 摘要
     *
     * @param data      待计算数据
     * @param key       密钥
     * @param algorithm HMAC 算法
     * @return Hex 编码的 HMAC 结果
     */
    String hmac(byte[] data, byte[] key, DigestAlgorithm algorithm);

    /**
     * 计算字符串的 HMAC
     *
     * @param data      待计算字符串
     * @param key       密钥字符串
     * @param algorithm HMAC 算法
     * @return Hex 编码的 HMAC 结果
     */
    String hmac(String data, String key, DigestAlgorithm algorithm);
}
