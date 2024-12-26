package com.github.loadup.components.gateway.core.model;

/**
 * 安全组件操作类型、算法与证书类型的映射关系
 */
public class CertAlgorithmConfig extends BaseAlgoConfig {

    /**
     * 算法类型
     */
    private String algoType;

    /**
     * Getter method for property <tt>algoType<tt>.
     */
    public String getAlgoType() {
        return algoType;
    }

    /**
     * Setter method for property <tt>algoType<tt>.
     */
    public void setAlgoType(String algoType) {
        this.algoType = algoType;
    }
}
