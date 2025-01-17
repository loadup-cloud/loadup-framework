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

/**
 * 报文数据表现格式，可以根据这个格式完成相应的报文读取
 */
public enum MessageFormat {

    /**
     * 文本
     */
    TEXT,

    /**
     * 字节
     */
    BYTE,

    /**
     * MAP结构
     */
    MAP,

    /**
     * multipart请求, post请求的特殊形式
     */
    MULTI_PART,

    /**
     * generic object
     */
    GENERIC;

    /**
     * 根据枚举代码，获取枚举
     */
    public static MessageFormat getEnumByCode(String code) {
        for (MessageFormat format : MessageFormat.values()) {
            if (format.getCode().equalsIgnoreCase(code)) {
                return format;
            }
        }

        return TEXT;
    }

    /**
     * 枚举名称
     */
    public String getCode() {
        return this.name();
    }

}
