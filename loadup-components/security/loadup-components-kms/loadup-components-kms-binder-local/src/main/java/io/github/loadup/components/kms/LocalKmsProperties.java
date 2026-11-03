/*
 * #%L
 * LoadUp Components KMS binder-local
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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.kms.local")
public class LocalKmsProperties {
    private String keyStoreLocation;
    private String keyStoreType = "PKCS12";
    private String keyStorePasswordEnv = "KMS_KEYSTORE_PASSWORD";
    private String keyPasswordEnv;

    public String getKeyStoreLocation() {
        return keyStoreLocation;
    }

    public void setKeyStoreLocation(String keyStoreLocation) {
        this.keyStoreLocation = keyStoreLocation;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyStorePasswordEnv() {
        return keyStorePasswordEnv;
    }

    public void setKeyStorePasswordEnv(String keyStorePasswordEnv) {
        this.keyStorePasswordEnv = keyStorePasswordEnv;
    }

    public String getKeyPasswordEnv() {
        return keyPasswordEnv;
    }

    public void setKeyPasswordEnv(String keyPasswordEnv) {
        this.keyPasswordEnv = keyPasswordEnv;
    }
}
