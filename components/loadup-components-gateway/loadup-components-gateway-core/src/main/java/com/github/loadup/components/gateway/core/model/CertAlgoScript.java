package com.github.loadup.components.gateway.core.model;

import org.apache.commons.lang3.StringUtils;

/**
 * 安全组件Groovy算法配置
 */
public class CertAlgoScript {

    /**
     * 类名
     */
    private String className;

    /**
     * 算法脚本内容
     */
    private String algoScriptContent;

    /**
     * Getter method for property <tt>className<tt>.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Setter method for property <tt>className<tt>.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Getter method for property <tt>algoScriptContent<tt>.
     */
    public String getAlgoScriptContent() {
        return algoScriptContent;
    }

    /**
     * Setter method for property <tt>algoScriptContent<tt>.
     */
    public void setAlgoScriptContent(String algoScriptContent) {
        this.algoScriptContent = algoScriptContent;
    }

    /**
     * 判断两个变量脚本内容是否相同
     */
    public boolean sameScriptContent(CertAlgoScript another) {
        return StringUtils.equals(this.algoScriptContent, another.getAlgoScriptContent());
    }
}