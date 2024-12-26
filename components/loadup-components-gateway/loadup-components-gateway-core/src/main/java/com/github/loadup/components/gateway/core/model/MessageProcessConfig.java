package com.github.loadup.components.gateway.core.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 通用报文处理配置
 */
public class MessageProcessConfig {

    /**
     * 报文处理id
     */
    private String messageProcessId;

    /**
     * 解析脚本
     */
    private String parserTemplate;

    /**
     * 解析脚本类名
     */
    private String parserClassName;

    /**
     * 组装脚本类型
     */
    private String assembleType;

    /**
     * 组装主脚本
     */
    private String assembleTemplate;
    /**
     * 解析脚本类名
     */
    private String assembleClassName;

    /**
     * 组装子脚本
     */
    private String assembleSubTemplate;

    /**
     * 组装扩展脚本
     */
    private String assembleExtTemplate;

    /**
     * 报文头脚本
     */
    private String headerTemplate;

    /**
     * 错误组装主脚本
     */
    private String errorTemplate;

    /**
     * 错误组装子脚本
     */
    private String errorSubTemplate;

    /**
     * 校验模板
     */
    private String validateTemplate;

    /**
     * 敏感字段域
     */
    private String sensitiveFields;

    /**
     * Getter method for property <tt>parserTemplate</tt>.
     */
    public String getParserTemplate() {
        return parserTemplate;
    }

    /**
     * Setter method for property <tt>parserTemplate</tt>.
     */
    public void setParserTemplate(String parserTemplate) {
        this.parserTemplate = parserTemplate;
    }

    /**
     * Getter method for property <tt>headerTemplate</tt>.
     */
    public String getHeaderTemplate() {
        return headerTemplate;
    }

    /**
     * Setter method for property <tt>headerTemplate</tt>.
     */
    public void setHeaderTemplate(String headerTemplate) {
        this.headerTemplate = headerTemplate;
    }

    /**
     * Getter method for property <tt>validateTemplate</tt>.
     */
    public String getValidateTemplate() {
        return validateTemplate;
    }

    /**
     * Setter method for property <tt>validateTemplate</tt>.
     */
    public void setValidateTemplate(String validateTemplate) {
        this.validateTemplate = validateTemplate;
    }

    /**
     * Getter method for property <tt>sensitiveFields</tt>.
     */
    public String getSensitiveFields() {
        return sensitiveFields;
    }

    /**
     * Setter method for property <tt>sensitiveFields</tt>.
     */
    public void setSensitiveFields(String sensitiveFields) {
        this.sensitiveFields = sensitiveFields;
    }

    /**
     * Getter method for property <tt>messageProcessId</tt>.
     */
    public String getMessageProcessId() {
        return messageProcessId;
    }

    /**
     * Setter method for property <tt>messageProcessId</tt>.
     */
    public void setMessageProcessId(String messageProcessId) {
        this.messageProcessId = messageProcessId;
    }

    /**
     * Getter method for property <tt>assembleTemplate</tt>.
     */
    public String getAssembleTemplate() {
        return assembleTemplate;
    }

    /**
     * Setter method for property <tt>assembleTemplate</tt>.
     */
    public void setAssembleTemplate(String assembleTemplate) {
        this.assembleTemplate = assembleTemplate;
    }

    /**
     * Getter method for property <tt>assembleSubTemplate</tt>.
     */
    public String getAssembleSubTemplate() {
        return assembleSubTemplate;
    }

    /**
     * Setter method for property <tt>assembleSubTemplate</tt>.
     */
    public void setAssembleSubTemplate(String assembleSubTemplate) {
        this.assembleSubTemplate = assembleSubTemplate;
    }

    /**
     * Getter method for property <tt>assembleExtTemplate</tt>.
     */
    public String getAssembleExtTemplate() {
        return assembleExtTemplate;
    }

    /**
     * Setter method for property <tt>assembleExtTemplate</tt>.
     */
    public void setAssembleExtTemplate(String assembleExtTemplate) {
        this.assembleExtTemplate = assembleExtTemplate;
    }

    /**
     * Getter method for property <tt>errorTemplate</tt>.
     */
    public String getErrorTemplate() {
        return errorTemplate;
    }

    /**
     * Setter method for property <tt>errorTemplate</tt>.
     */
    public void setErrorTemplate(String errorTemplate) {
        this.errorTemplate = errorTemplate;
    }

    /**
     * Getter method for property <tt>errorSubTemplate</tt>.
     */
    public String getErrorSubTemplate() {
        return errorSubTemplate;
    }

    /**
     * Setter method for property <tt>errorSubTemplate</tt>.
     */
    public void setErrorSubTemplate(String errorSubTemplate) {
        this.errorSubTemplate = errorSubTemplate;
    }

    /**
     * Getter method for property <tt>parserClassName</tt>.
     */
    public String getParserClassName() {
        return parserClassName;
    }

    /**
     * Setter method for property <tt>parserClassName</tt>.
     */
    public void setParserClassName(String parserClassName) {
        this.parserClassName = parserClassName;
    }

    /**
     * Getter method for property <tt>assembleType</tt>.
     */
    public String getAssembleType() {
        return assembleType;
    }

    /**
     * Setter method for property <tt>assembleType</tt>.
     */
    public void setAssembleType(String assembleType) {
        this.assembleType = assembleType;
    }

    public String getAssembleClassName() {
        return assembleClassName;
    }

    public void setAssembleClassName(String assembleClassName) {
        this.assembleClassName = assembleClassName;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
