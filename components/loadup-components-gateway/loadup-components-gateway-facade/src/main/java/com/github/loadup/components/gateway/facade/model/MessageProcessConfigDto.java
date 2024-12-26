package com.github.loadup.components.gateway.facade.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * <p>
 * MessageProcessConfig.java
 * </p>
 */
public class MessageProcessConfigDto {

    /**
     * Primary Key
     */
    private String messageProcessId;

    /**
     * Required
     */
    private String parserTemplate;

    /**
     * Required
     */
    private String parserClassName;

    /**
     * Required
     */
    private String assembleTemplate;
    private String assembleClassName;

    /**
     * Optional
     */
    private String headerTemplate;

    /**
     * Gets get message process id.
     */
    public String getMessageProcessId() {
        return messageProcessId;
    }

    /**
     * Sets set message process id.
     */
    public void setMessageProcessId(String messageProcessId) {
        this.messageProcessId = messageProcessId;
    }

    /**
     * Gets get parser template.
     */
    public String getParserTemplate() {
        return parserTemplate;
    }

    /**
     * Sets set parser template.
     */
    public void setParserTemplate(String parserTemplate) {
        this.parserTemplate = parserTemplate;
    }

    /**
     * Gets get parser class name.
     */
    public String getParserClassName() {
        return parserClassName;
    }

    /**
     * Sets set parser class name.
     */
    public void setParserClassName(String parserClassName) {
        this.parserClassName = parserClassName;
    }

    /**
     * Gets get assemble template.
     */
    public String getAssembleTemplate() {
        return assembleTemplate;
    }

    /**
     * Sets set assemble template.
     */
    public void setAssembleTemplate(String assembleTemplate) {
        this.assembleTemplate = assembleTemplate;
    }

    /**
     * Gets get header template.
     */
    public String getHeaderTemplate() {
        return headerTemplate;
    }

    /**
     * Sets set header template.
     */
    public void setHeaderTemplate(String headerTemplate) {
        this.headerTemplate = headerTemplate;
    }

    public String getAssembleClassName() {
        return assembleClassName;
    }

    public void setAssembleClassName(String assembleClassName) {
        this.assembleClassName = assembleClassName;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}