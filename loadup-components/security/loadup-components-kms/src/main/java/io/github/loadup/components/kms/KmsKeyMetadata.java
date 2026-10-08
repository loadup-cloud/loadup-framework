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

import java.util.Map;

/** Public metadata; the map contains versioned public keys, never private or symmetric key material. */
public record KmsKeyMetadata(
        String name,
        String type,
        int latestVersion,
        int minEncryptionVersion,
        int minDecryptionVersion,
        boolean supportsEncryption,
        boolean supportsDecryption,
        boolean supportsSigning,
        Map<Integer, String> publicKeys) {
    public KmsKeyMetadata {
        publicKeys = Map.copyOf(publicKeys);
    }
}
