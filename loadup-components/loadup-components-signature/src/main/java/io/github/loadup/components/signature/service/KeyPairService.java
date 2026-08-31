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

import io.github.loadup.components.signature.enums.KeyAlgorithm;
import io.github.loadup.components.signature.model.KeyPairInfo;
import java.security.PrivateKey;
import java.security.PublicKey;

/** Facade for key pair generation and Base64 key loading. */
public interface KeyPairService {

    /**
     * Generates a key pair for the given algorithm and size.
     *
     * @param algorithm the key algorithm
     * @param keySize the key size in bits
     * @return the key pair info with Base64-encoded keys
     */
    KeyPairInfo generateKeyPair(KeyAlgorithm algorithm, int keySize);

    /**
     * Generates a key pair using the configured default key size.
     *
     * @param algorithm the key algorithm
     * @return the key pair info
     */
    KeyPairInfo generateKeyPair(KeyAlgorithm algorithm);

    /**
     * Loads a private key from its Base64-encoded PKCS#8 representation.
     *
     * @param base64PrivateKey the Base64-encoded private key
     * @param algorithm the key algorithm
     * @return the private key
     */
    PrivateKey loadPrivateKey(String base64PrivateKey, KeyAlgorithm algorithm);

    /**
     * Loads a public key from its Base64-encoded X.509 representation.
     *
     * @param base64PublicKey the Base64-encoded public key
     * @param algorithm the key algorithm
     * @return the public key
     */
    PublicKey loadPublicKey(String base64PublicKey, KeyAlgorithm algorithm);
}
