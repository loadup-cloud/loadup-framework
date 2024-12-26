package com.github.loadup.components.gateway.certification.model;

import org.apache.commons.lang3.StringUtils;

/**
 * 证书内容类型枚举
 */
public enum CertContentType {

    /**
     * 证书内容
     */
    CERT_OFFICIAL_CONTENT("CERT_OFFICIAL_CONTENT", "证书正式内容"),

    /**
     * 证书别名
     */
    CERT_ALIAS_NAME("CERT_ALIAS_NAME", "证书别名"),

    /**
     * kmiKey别名
     */
    KMI_KEY_ALIAS_NAME("KMI_KEY_ALIAS_NAME", "kmiKey别名"),
    ;

    /**
     * 证书内容类型
     */
    private String certContentType;

    /**
     * 描述
     */
    private String desc;

    /**
     * 基于String类型获取枚举
     */
    public static CertContentType getByName(String certContentType) {
        for (CertContentType contentType : CertContentType.values()) {
            if (StringUtils.equals(certContentType, contentType.getCertContentType())) {
                return contentType;
            }
        }
        return null;
    }

    /**
     * 构造函数
     */
    CertContentType(String certContentType, String desc) {
        this.certContentType = certContentType;
        this.desc = desc;
    }

    /**
     * Getter method for property <tt>certContentType<tt>.
     */
    public String getCertContentType() {
        return certContentType;
    }

    /**
     * Setter method for property <tt>certContentType<tt>.
     */
    public void setCertContentType(String certContentType) {
        this.certContentType = certContentType;
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
