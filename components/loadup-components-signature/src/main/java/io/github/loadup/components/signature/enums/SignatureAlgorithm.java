package io.github.loadup.components.signature.enums;

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

import lombok.Getter;

/**
 * 签名算法枚举
 *
 * @author loadup
 */
@Getter
public enum SignatureAlgorithm {

    /**
     * RSA with SHA-256
     */
    SHA256_WITH_RSA("SHA256withRSA", "RSA"),

    /**
     * RSA with SHA-512
     */
    SHA512_WITH_RSA("SHA512withRSA", "RSA"),

    /**
     * DSA with SHA-256
     */
    SHA256_WITH_DSA("SHA256withDSA", "DSA"),

    /**
     * ECDSA with SHA-256
     */
    SHA256_WITH_ECDSA("SHA256withECDSA", "EC");

    /**
     * JCA 算法名称
     */
    private final String jcaName;

    /**
     * 密钥算法
     */
    private final String keyAlgorithm;

    SignatureAlgorithm(String jcaName, String keyAlgorithm) {
        this.jcaName = jcaName;
        this.keyAlgorithm = keyAlgorithm;
    }
}
