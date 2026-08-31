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

/** Facade for asymmetric (RSA / DSA / ECDSA) sign and verify operations. */
public interface SignatureService {

    /**
     * Signs the given data with the private key.
     *
     * @param data the data to sign
     * @param privateKey the private key
     * @param algorithm the signature algorithm
     * @return the Base64-encoded signature
     */
    String sign(byte[] data, PrivateKey privateKey, SignatureAlgorithm algorithm);

    /**
     * Signs the given string with a Base64-encoded private key.
     *
     * @param data the data to sign
     * @param privateKeyBase64 the Base64-encoded private key
     * @param algorithm the signature algorithm
     * @return the Base64-encoded signature
     */
    String sign(String data, String privateKeyBase64, SignatureAlgorithm algorithm);

    /**
     * Verifies the signature with the public key.
     *
     * @param data the original data
     * @param signatureBase64 the Base64-encoded signature
     * @param publicKey the public key
     * @param algorithm the signature algorithm
     * @return {@code true} when the signature is valid
     */
    boolean verify(byte[] data, String signatureBase64, PublicKey publicKey, SignatureAlgorithm algorithm);

    /**
     * Verifies the signature with a Base64-encoded public key.
     *
     * @param data the original string
     * @param signatureBase64 the Base64-encoded signature
     * @param publicKeyBase64 the Base64-encoded public key
     * @param algorithm the signature algorithm
     * @return {@code true} when the signature is valid
     */
    boolean verify(String data, String signatureBase64, String publicKeyBase64, SignatureAlgorithm algorithm);
}
