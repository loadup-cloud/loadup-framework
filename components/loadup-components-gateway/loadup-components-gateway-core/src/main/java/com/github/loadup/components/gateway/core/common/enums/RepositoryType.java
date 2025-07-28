/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.core.common.enums;

/*-
 * #%L
 * loadup-components-gateway-core
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 *
 */
@Getter
@AllArgsConstructor
public enum RepositoryType {

    /**
     * file
     */
    FILE("FILE", true, "file repository, config in gateway local cache"),

    /**
     * database
     */
    DATABASE("DATABASE", true, "database repository, config in gateway local cache"),

    /**
     * config center
     */
    CONFIG_CENTER("CONFIG_CENTER", false, "config center repository, config in remote query client"),
    ;

    /**
     * code
     */
    private String code;

    /**
     * whether config in  internal cache
     */
    private Boolean cacheable;

    /**
     * message
     */
    private String message;

    /**
     * getByCode
     * @return RepositoryType, default RepositoryType.FILE
     */
    public static RepositoryType getByCode(String code) {
        return Arrays.stream(RepositoryType.values()).filter(status -> status.getCode().equalsIgnoreCase(code)).findFirst().orElse(
                RepositoryType.FILE);
    }

    /**
     * isCached or not
     */
    public boolean isCacheable() {
        return cacheable;
    }

}
