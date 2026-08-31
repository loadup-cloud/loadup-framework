package io.github.loadup.components.signature.service;

/*-
 * #%L
 * LoadUp Components :: Signature
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
