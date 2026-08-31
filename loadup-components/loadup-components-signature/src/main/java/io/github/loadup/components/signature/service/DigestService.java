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

/** Facade for digest (MD5 / SHA) and HMAC computation. */
public interface DigestService {

    /**
     * Computes the digest of the given bytes.
     *
     * @param data the input bytes
     * @param algorithm the digest algorithm
     * @return the hex-encoded digest
     */
    String digest(byte[] data, DigestAlgorithm algorithm);

    /**
     * Computes the digest of the given string.
     *
     * @param data the input string
     * @param algorithm the digest algorithm
     * @return the hex-encoded digest
     */
    String digest(String data, DigestAlgorithm algorithm);

    /**
     * Computes the HMAC of the given bytes.
     *
     * @param data the input bytes
     * @param key the secret key
     * @param algorithm the HMAC algorithm
     * @return the hex-encoded MAC
     */
    String hmac(byte[] data, byte[] key, DigestAlgorithm algorithm);

    /**
     * Computes the HMAC of the given string.
     *
     * @param data the input string
     * @param key the secret key
     * @param algorithm the HMAC algorithm
     * @return the hex-encoded MAC
     */
    String hmac(String data, String key, DigestAlgorithm algorithm);
}
