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

/** The initial supported Transit key types. SM2 and SM4 are not supported by this backend. */
public enum KmsKeyType {
    RSA_2048("rsa-2048"),
    RSA_3072("rsa-3072"),
    RSA_4096("rsa-4096"),
    AES_256_GCM("aes256-gcm96");
    private final String transitName;

    KmsKeyType(String transitName) {
        this.transitName = transitName;
    }

    public String transitName() {
        return transitName;
    }
}
