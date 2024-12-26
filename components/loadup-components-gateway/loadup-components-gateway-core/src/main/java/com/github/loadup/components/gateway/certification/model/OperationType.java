package com.github.loadup.components.gateway.certification.model;

/**
 * 操作类型
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

    /**
     * 报文 摘要
     */
    OP_DIGEST("OP_DIGEST", "摘要"),

    /**
     * 报文 特殊签名(katong类报文)
     */
    OP_ENCODE("OP_ENCODE", "特殊签名"),

    /**
     * 报文 特殊验签(katong类报文)
     */
    OP_DECODE("OP_DECODE", "特殊验签"),

    /**
     * 文件加密
     */
    FILE_ENCRYPT("FILE_ENCRYPT", "文件加密"),

    /**
     * 文件解密
     */
    FILE_DECRYPT("FILE_DECRYPT", "文件解密"),

    /**
     * 文件签名
     */
    FILE_SIGN("FILE_SIGN", "文件签名"),

    /**
     * 文件验签
     */
    FILE_VERIFY("FILE_SIGN", "文件签名"),

    /**
     * 文件摘要
     */
    FILE_DIGEST("FILE_SIGN", "文件摘要"),

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
     * 构造函数
     */
    OperationType(String name, String desc) {
        this.name = name;
        this.desc = desc;
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
}
