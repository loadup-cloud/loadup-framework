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
 * 密钥算法枚举
 *
 * @author loadup
 */
@Getter
public enum KeyAlgorithm {

    /**
     * RSA 算法
     */
    RSA("RSA", 2048),

    /**
     * DSA 算法
     */
    DSA("DSA", 2048),

    /**
     * ECDSA (椭圆曲线) 算法
     */
    EC("EC", 256);

    /**
     * JCA 算法名称
     */
    private final String jcaName;

    /**
     * 默认密钥长度
     */
    private final int defaultKeySize;

    KeyAlgorithm(String jcaName, int defaultKeySize) {
        this.jcaName = jcaName;
        this.defaultKeySize = defaultKeySize;
    }
}
