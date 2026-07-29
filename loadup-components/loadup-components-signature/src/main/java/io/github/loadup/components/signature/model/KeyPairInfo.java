package io.github.loadup.components.signature.model;

/*-
 * #%L
 * LoadUp Components :: Signature
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.Serializable;

/**
 * 密钥对信息
 *
 * @author loadup
 */
public class KeyPairInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Base64 编码的公钥
     */
    private String publicKey;

    /**
     * Base64 编码的私钥
     */
    private String privateKey;

    /**
     * 密钥算法
     */
    private String algorithm;

    /**
     * 密钥长度
     */
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
