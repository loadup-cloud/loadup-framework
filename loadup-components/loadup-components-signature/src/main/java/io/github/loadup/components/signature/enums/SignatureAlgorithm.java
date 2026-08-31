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
 * 签名算法枚举
 *
 * @author loadup
 */
public enum SignatureAlgorithm {

    /**
     * RSA with SHA-256
     */
    SHA256_WITH_RSA("SHA256withRSA", "RSA"),

    /**
     * RSA with SHA-512
     */
    SHA512_WITH_RSA("SHA512withRSA", "RSA"),

    /**
     * DSA with SHA-256
     */
    SHA256_WITH_DSA("SHA256withDSA", "DSA"),

    /**
     * ECDSA with SHA-256
     */
    SHA256_WITH_ECDSA("SHA256withECDSA", "EC");

    /**
     * JCA 算法名称
     */
    private final String jcaName;

    /**
     * 密钥算法
     */
    private final String keyAlgorithm;

    SignatureAlgorithm(String jcaName, String keyAlgorithm) {
        this.jcaName = jcaName;
        this.keyAlgorithm = keyAlgorithm;
    }

    public String getJcaName() {
        return this.jcaName;
    }

    public String getKeyAlgorithm() {
        return this.keyAlgorithm;
    }
}
