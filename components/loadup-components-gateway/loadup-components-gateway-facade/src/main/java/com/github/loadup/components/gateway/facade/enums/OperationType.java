package com.github.loadup.components.gateway.facade.enums;

/*-
 * #%L
 * loadup-components-gateway-facade
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
 *
 */
public enum OperationType {

    /**
     * 报文 签名操作
     */
    OP_SIGN("OP_SIGN", "签名"),

    /**
     * 报文 验签操作
     */
    OP_VERIFY("OP_VERIFY", "验签"),

    /**
     * 报文 加密
     */
    OP_ENCRYPT("OP_ENCRYPT", "加密"),

    /**
     * 报文 解密
     */
    OP_DECRYPT("OP_DECRYPT", "解密"),
    ;

    /**
     * 操作名字
     */
    private String name;

    /**
     * 描述
     */
    private String desc;

    /**
     * 构造函数
     */
    OperationType(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    /**
     * 基于操作类型获取具体枚举值
     */
    public static OperationType getByName(String operationType) {
        for (OperationType type : OperationType.values()) {
            if (type.getName().equals(operationType)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * 
     */
    public String getName() {
        return name;
    }

    /**
     * 
     */
    public void setName(String name) {
        this.name = name;
    }
}
