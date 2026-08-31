package io.github.loadup.components.signature.enums;

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

/**
 * 密钥算法枚举
 *
 * @author loadup
 */
public enum KeyAlgorithm {

    /**
     * RSA 算法
     */
    RSA("RSA", 2048),

    /**
     * DSA 算法
     */
    DSA("DSA", 2048),

    /**
     * ECDSA (椭圆曲线) 算法
     */
    EC("EC", 256);

    /**
     * JCA 算法名称
     */
    private final String jcaName;

    /**
     * 默认密钥长度
     */
    private final int defaultKeySize;

    KeyAlgorithm(String jcaName, int defaultKeySize) {
        this.jcaName = jcaName;
        this.defaultKeySize = defaultKeySize;
    }

    public String getJcaName() {
        return this.jcaName;
    }

    public int getDefaultKeySize() {
        return this.defaultKeySize;
    }
}
