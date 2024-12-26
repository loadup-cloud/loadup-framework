package com.github.loadup.components.gateway.core.common.enums;

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
     * 枚举名称
     */
    public String getCode() {
        return this.name();
    }

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

}
