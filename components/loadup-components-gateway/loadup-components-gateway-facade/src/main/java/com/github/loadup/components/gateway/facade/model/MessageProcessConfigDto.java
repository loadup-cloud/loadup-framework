package com.github.loadup.components.gateway.facade.model;

/*-
 * #%L
 * loadup-components-gateway-facade
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
