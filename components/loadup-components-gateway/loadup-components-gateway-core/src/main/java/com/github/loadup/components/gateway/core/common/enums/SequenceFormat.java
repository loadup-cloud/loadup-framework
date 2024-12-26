package com.github.loadup.components.gateway.core.common.enums;

/**
 * sequence生成样式枚举
 */
public enum SequenceFormat {
    /**
     * 字符串型SEQ - 样式为:YYYYMMDD+SEQ,16位
     */
    SEQ_DATE("SEQ_DATE", "样式为:YYYYMMDD+SEQ,16位"),

    /**
     * Date+SEQ - yyyyMMddHHmmss+SEQ, 22chars
     */
    SEQ_DATE_LONG("SEQ_DATE_LONG", "format: yyyyMMddHHmmss+SEQ, 22chars"),

    /**
     * 数字型SEQ - 样式为：数字类型SEQ
     */
    SEQ_NUM_11("SEQ_NUM_11", "样式为：数字类型SEQ"),

    ;

    /**
     * 代码
     */
    private String code;

    /**
     * 描述
     */
    private String message;

    /**
     * 构造方法
     */
    private SequenceFormat(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Getter method for property <tt>code</tt>.
     */
    public String getCode() {
        return code;
    }

    /**
     * Getter method for property <tt>message</tt>.
     */
    public String getMessage() {
        return message;
    }

    /**
     * 根据代码获取枚举
     */
    public static SequenceFormat getEnumByCode(String code) {
        for (SequenceFormat status : SequenceFormat.values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }
}
