package com.github.loadup.components.gateway.core.common.enums;

/**
 * 通讯协议安全策略枚举
 */
public enum SecurityType {

    /**
     * 标准验签策略
     */
    VERIFY,
    /**
     * JWE 解密
     */
    JWE_DECODE,
    /**
     * 不做任何处理
     */
    NONE

}