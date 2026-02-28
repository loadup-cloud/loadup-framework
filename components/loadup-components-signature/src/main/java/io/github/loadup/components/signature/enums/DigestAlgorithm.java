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
 * 摘要算法枚举
 *
 * @author loadup
 */
@Getter
public enum DigestAlgorithm {

    /**
     * MD5 (不推荐用于安全场景)
     */
    MD5("MD5", false),

    /**
     * SHA-1 (不推荐用于安全场景)
     */
    SHA1("SHA-1", false),

    /**
     * SHA-256
     */
    SHA256("SHA-256", false),

    /**
     * SHA-512
     */
    SHA512("SHA-512", false),

    /**
     * HMAC with SHA-256
     */
    HMAC_SHA256("HmacSHA256", true),

    /**
     * HMAC with SHA-512
     */
    HMAC_SHA512("HmacSHA512", true);

    /**
     * JCA 算法名称
     */
    private final String jcaName;

    /**
     * 是否为 HMAC 算法
     */
    private final boolean isHmac;

    DigestAlgorithm(String jcaName, boolean isHmac) {
        this.jcaName = jcaName;
        this.isHmac = isHmac;
    }
}
