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

import io.github.loadup.components.signature.enums.KeyAlgorithm;
import io.github.loadup.components.signature.model.KeyPairInfo;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 密钥对管理服务接口
 *
 * @author loadup
 */
public interface KeyPairService {

    /**
     * 生成密钥对
     *
     * @param algorithm 密钥算法
     * @param keySize   密钥长度
     * @return 密钥对信息（包含 Base64 编码的公私钥）
     */
    KeyPairInfo generateKeyPair(KeyAlgorithm algorithm, int keySize);

    /**
     * 生成密钥对（使用默认密钥长度）
     *
     * @param algorithm 密钥算法
     * @return 密钥对信息
     */
    KeyPairInfo generateKeyPair(KeyAlgorithm algorithm);

    /**
     * 从 Base64 字符串加载私钥
     *
     * @param base64PrivateKey Base64 编码的私钥
     * @param algorithm        密钥算法
     * @return 私钥对象
     */
    PrivateKey loadPrivateKey(String base64PrivateKey, KeyAlgorithm algorithm);

    /**
     * 从 Base64 字符串加载公钥
     *
     * @param base64PublicKey Base64 编码的公钥
     * @param algorithm       密钥算法
     * @return 公钥对象
     */
    PublicKey loadPublicKey(String base64PublicKey, KeyAlgorithm algorithm);
}
