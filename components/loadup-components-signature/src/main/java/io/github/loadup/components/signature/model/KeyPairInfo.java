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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 密钥对信息
 *
 * @author loadup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
