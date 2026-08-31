package io.github.loadup.components.signature.model;

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

import java.io.Serializable;

/** Immutable key pair container with Base64-encoded keys. */
public class KeyPairInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The Base64-encoded public key. */
    private String publicKey;

    /** The Base64-encoded private key. */
    private String privateKey;

    /** The key algorithm. */
    private String algorithm;

    /** The key size in bits. */
    private Integer keySize;

    public KeyPairInfo(String publicKey, String privateKey, String algorithm, Integer keySize) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.algorithm = algorithm;
        this.keySize = keySize;
    }

    public KeyPairInfo() {}

    public String getPublicKey() {
        return this.publicKey;
    }

    public String getPrivateKey() {
        return this.privateKey;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public Integer getKeySize() {
        return this.keySize;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String publicKey;
        private String privateKey;
        private String algorithm;
        private Integer keySize;

        public Builder publicKey(String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public Builder privateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public Builder algorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public Builder keySize(Integer keySize) {
            this.keySize = keySize;
            return this;
        }

        public KeyPairInfo build() {
            return new KeyPairInfo(this.publicKey, this.privateKey, this.algorithm, this.keySize);
        }
    }

    public long getSerialVersionUID() {
        return this.serialVersionUID;
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
