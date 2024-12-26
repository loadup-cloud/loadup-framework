package com.github.loadup.components.gateway.core.model;

/**
 *
 */
public class DynamicScriptBeanConfig {

    /**
     * bean name
     */
    private String beanName;

    /**
     * bean content
     */
    private String beanContent;

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanContent() {
        return beanContent;
    }

    public void setBeanContent(String beanContent) {
        this.beanContent = beanContent;
    }
}