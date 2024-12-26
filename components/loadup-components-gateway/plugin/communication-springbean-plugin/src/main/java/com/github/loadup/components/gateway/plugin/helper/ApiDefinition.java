package com.github.loadup.components.gateway.plugin.helper;

/**
 *
 */
public class ApiDefinition {

    public ApiDefinition(String beanId, String method) {
        this.beanId = beanId;
        this.method = method;
    }

    private String beanId;

    private String method;

    public String getBeanId() {
        return beanId;
    }

    public void setBeanId(String beanId) {
        this.beanId = beanId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "ApiDefinition{" +
                "beanId='" + beanId + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
