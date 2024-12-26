package com.github.loadup.components.gateway.core.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public enum TemplateType {

    /**
     * parser
     */
    PARSER("PARSER", "parser template"),

    /**
     * assembler
     */
    ASSEMBLER("ASSEMBLER", "assembler template"),

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
     * 构造函数
     */
    TemplateType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据名字获取编码的枚举类型
     */
    public static TemplateType getByCode(String code) {
        for (TemplateType templateType : TemplateType.values()) {
            if (StringUtils.equalsIgnoreCase(templateType.getCode(), code)) {
                return templateType;
            }
        }
        return null;
    }

    /**
     * Getter method for property <tt>code</tt>.
     */
    public String getCode() {
        return code;
    }

    /**
     * Setter method for property <tt>code</tt>.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Getter method for property <tt>desc</tt>.
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Setter method for property <tt>desc</tt>.
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }
}