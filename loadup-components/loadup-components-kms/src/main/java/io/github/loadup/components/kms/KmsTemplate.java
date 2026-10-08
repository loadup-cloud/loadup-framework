/*
 * #%L
 * LoadUp Kms
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
package io.github.loadup.components.kms;

/** Cryptographic operations authorized by the runtime OpenBao identity. */
public interface KmsTemplate {
    KmsCiphertext encrypt(KmsKeyRef key, byte[] plaintext);

    byte[] decrypt(KmsKeyRef expectedKey, KmsCiphertext ciphertext);

    KmsSignature sign(KmsKeyRef key, KmsSignatureAlgorithm algorithm, byte[] message);

    boolean verify(KmsKeyRef expectedKey, KmsSignature signature, byte[] message);

    KmsKeyMetadata metadata(String name);

    String publicKey(KmsKeyRef key);
}
