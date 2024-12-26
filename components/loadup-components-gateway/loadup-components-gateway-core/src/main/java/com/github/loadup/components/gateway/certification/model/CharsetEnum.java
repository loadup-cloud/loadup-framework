package com.github.loadup.components.gateway.certification.model;

import org.apache.commons.lang3.StringUtils;

/**
 * 编码枚举类
 */
public enum CharsetEnum {

    /**
     * utf-8 编码
     */
    UTF8("UTF-8"),

    /**
     * gbk编码
     */
    GBK("GBK"),

    /**
     * ISO-8859-1编码
     */
    ISO_8859_1("ISO-8859-1"),

    /**
     * GB2312
     */
    GB2312("GB2312"),
    ;

    /**
     * 编码
     */
    private String charSet;

    /**
     * 构造函数
     */
    private CharsetEnum(String charSet) {
        this.charSet = charSet;
    }

    /**
     * 根据String获取对应编码枚举
     */
    public static CharsetEnum getByName(String input) {
        for (CharsetEnum charsetEnum : CharsetEnum.values()) {
            if (StringUtils.equals(input, charsetEnum.getCharSet())) {
                return charsetEnum;
            }
        }
        return null;
    }

    /**
     * Getter method for property <tt>charSet<tt>.
     */
    public String getCharSet() {
        return charSet;
    }

    /**
     * Setter method for property <tt>charSet<tt>.
     */
    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }
}
