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

/** Supported asymmetric key algorithms. */
public enum KeyAlgorithm {

    /**
     * RSA.
     */
    RSA("RSA", 2048),

    /**
     * DSA.
     */
    DSA("DSA", 2048),

    /**
     * ECDSA (elliptic curve).
     */
    EC("EC", 256);

    /**
     * The JCA algorithm name.
     */
    private final String jcaName;

    /**
     * The default key size in bits.
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
