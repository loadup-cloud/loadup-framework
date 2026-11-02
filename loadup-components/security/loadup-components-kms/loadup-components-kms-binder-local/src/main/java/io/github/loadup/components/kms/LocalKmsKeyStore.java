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

import java.security.KeyStore;
import java.util.Objects;

/** KeyStore and common entry password supplied by the application. */
public record LocalKmsKeyStore(KeyStore store, char[] password) {
    public LocalKmsKeyStore {
        Objects.requireNonNull(store);
        password = Objects.requireNonNull(password).clone();
    }

    @Override public char[] password() { return password.clone(); }
}
