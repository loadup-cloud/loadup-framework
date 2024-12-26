package com.github.loadup.components.gateway.certification.model;

/**
 * 编码类型
 */
public enum FormatType {

    /**
     * base64编码
     */
    FORMAT_BASE64("FORMAT_BASE64", "BASE64编码"),

    /**
     * urlBase64编码
     */
    FORMAT_URLBASE64("FORMAT_URLBASE64", "URLBASE64编码"),

    /**
     * hex编码
     */
    FORMAT_HEX("FORMAT_HEX", "HEX格式编码"),

    /**
     * 双重base64编码
     */
    DOUBLE_BASE64("DOUBLE_BASE64", "双重base64编码"),

    ;

    /**
     * 名字
     */
    private String name;

    /**
     * 描述
     */
    private String desc;

    /**
     * 构造函数
     */
    FormatType(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    /**
     * 根据名字获取编码的枚举类型
     */
    public static FormatType getByName(String name) {
        for (FormatType formatType : FormatType.values()) {
            if (formatType.getName().equals(name)) {
                return formatType;
            }
        }
        return null;
    }

    /**
     * Getter method for property <tt>name<tt>.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for property <tt>name<tt>.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter method for property <tt>desc<tt>.
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Setter method for property <tt>desc<tt>.
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
