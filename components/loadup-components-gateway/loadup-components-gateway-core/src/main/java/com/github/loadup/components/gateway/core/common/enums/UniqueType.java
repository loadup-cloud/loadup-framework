/**
 * Alipy.com Inc.
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
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

import org.apache.commons.lang3.StringUtils;

/**
 * <P>幂等类型枚举类</P>
 */
public enum UniqueType {

    /**
     * SERIAL_UNIQUE
     */
    SERIAL_UNIQUE("SERIAL_UNIQUE", "流水号幂等"),

    /**
     * FILE_SRC_PATH_UNIQUE
     */
    FILE_SRC_PATH_UNIQUE("FILE_SRC_PATH_UNIQUE", "源文件路径幂等"),

    /**
     * FILE_DEST_PATH_UNIQUE
     */
    FILE_DEST_PATH_UNIQUE("FILE_DEST_PATH_UNIQUE", "目标文件路径幂等"),
    ;

    /**
     * code
     */
    private String code;

    /**
     * desc
     */
    private String desc;

    /**
     * 构造器
     */
    private UniqueType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 通过code获取枚举类
     */
    public static UniqueType getByCode(String code) {

        for (UniqueType type : UniqueType.values()) {
            if (StringUtils.equalsIgnoreCase(type.getCode(), code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * getter
     */
    public String getCode() {
        return code;
    }

    /**
     * getter
     */
    public String getDesc() {
        return desc;
    }
}
