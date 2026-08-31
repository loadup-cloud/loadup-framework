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

/** Supported digest and HMAC algorithms. */
public enum DigestAlgorithm {

    /**
     * MD5 (not recommended for security-sensitive scenarios).
     */
    MD5("MD5", false),

    /**
     * SHA-1 (not recommended for security-sensitive scenarios).
     */
    SHA1("SHA-1", false),

    /**
     * SHA-256
     */
    SHA256("SHA-256", false),

    /**
     * SHA-512
     */
    SHA512("SHA-512", false),

    /**
     * HMAC with SHA-256
     */
    HMAC_SHA256("HmacSHA256", true),

    /**
     * HMAC with SHA-512
     */
    HMAC_SHA512("HmacSHA512", true);

    /** The JCA algorithm name. */
    private final String jcaName;

    /**
     * Whether this is an HMAC algorithm.
     */
    private final boolean hmac;

    DigestAlgorithm(String jcaName, boolean isHmac) {
        this.jcaName = jcaName;
        this.hmac = isHmac;
    }

    public String getJcaName() {
        return this.jcaName;
    }

    public boolean isHmac() {
        return hmac;
    }
}
