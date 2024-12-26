package com.github.loadup.components.gateway.core.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * CertAlogMap 和 CertAlgorithmConfig的抽象类
 */
public class BaseAlgoConfig {
    /**
     * certCode
     */
    private String certCode;

    /**
     * 操作类型
     */
    private String operateType;

    /**
     * 算法名称
     */
    private String algoName;

    /**
     * 证书类型,多个用 ";" 分隔
     */
    private String certTypes;

    /**
     * 算法属性 以key1=value1;key2=value2 形式存储
     */
    private String algoProperties;

    /**
     * 备注
     */
    private String memo;

    /**
     * Getter method for property <tt>algoName<tt>.
     */
    public String getAlgoName() {
        return algoName;
    }

    /**
     * Setter method for property <tt>algoName<tt>.
     */
    public void setAlgoName(String algoName) {
        this.algoName = algoName;
    }

    /**
     * Getter method for property <tt>algoProperties<tt>.
     */
    public String getAlgoProperties() {
        return algoProperties;
    }

    /**
     * Setter method for property <tt>algoProperties<tt>.
     */
    public void setAlgoProperties(String algoProperties) {
        this.algoProperties = algoProperties;
    }

    /**
     * Getter method for property <tt>certCode<tt>.
     */
    public String getCertCode() {
        return certCode;
    }

    /**
     * Setter method for property <tt>certCode<tt>.
     */
    public void setCertCode(String certCode) {
        this.certCode = certCode;
    }

    /**
     * Getter method for property <tt>certTypes<tt>.
     */
    public String getCertTypes() {
        return certTypes;
    }

    /**
     * Setter method for property <tt>certTypes<tt>.
     */
    public void setCertTypes(String certTypes) {
        this.certTypes = certTypes;
    }

    /**
     * Getter method for property <tt>memo<tt>.
     */
    public String getMemo() {
        return memo;
    }

    /**
     * Setter method for property <tt>memo<tt>.
     */
    public void setMemo(String memo) {
        this.memo = memo;
    }

    /**
     * Getter method for property <tt>operateType<tt>.
     */
    public String getOperateType() {
        return operateType;
    }

    /**
     * Setter method for property <tt>operateType<tt>.
     */
    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    /**
     * 转换为String
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}