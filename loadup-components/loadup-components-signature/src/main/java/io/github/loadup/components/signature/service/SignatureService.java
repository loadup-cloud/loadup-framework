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

import io.github.loadup.components.signature.enums.SignatureAlgorithm;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 签名服务接口
 *
 * @author loadup
 */
public interface SignatureService {

    /**
     * 使用私钥对数据进行签名
     *
     * @param data       待签名数据
     * @param privateKey 私钥
     * @param algorithm  签名算法
     * @return Base64 编码的签名结果
     */
    String sign(byte[] data, PrivateKey privateKey, SignatureAlgorithm algorithm);

    /**
     * 使用私钥对字符串进行签名
     *
     * @param data             待签名字符串
     * @param privateKeyBase64 Base64 编码的私钥
     * @param algorithm        签名算法
     * @return Base64 编码的签名结果
     */
    String sign(String data, String privateKeyBase64, SignatureAlgorithm algorithm);

    /**
     * 使用公钥验证签名
     *
     * @param data            原始数据
     * @param signatureBase64 Base64 编码的签名
     * @param publicKey       公钥
     * @param algorithm       签名算法
     * @return true=验证通过, false=验证失败
     */
    boolean verify(byte[] data, String signatureBase64, PublicKey publicKey, SignatureAlgorithm algorithm);

    /**
     * 使用公钥验证签名（字符串形式）
     *
     * @param data            原始字符串
     * @param signatureBase64 Base64 编码的签名
     * @param publicKeyBase64 Base64 编码的公钥
     * @param algorithm       签名算法
     * @return true=验证通过, false=验证失败
     */
    boolean verify(String data, String signatureBase64, String publicKeyBase64, SignatureAlgorithm algorithm);
}
