package com.github.loadup.components.gateway.core.model;

/*-
 * #%L
 * loadup-components-gateway-core
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
     * 
     */
    public String getParserTemplate() {
        return parserTemplate;
    }

    /**
     * 
     */
    public void setParserTemplate(String parserTemplate) {
        this.parserTemplate = parserTemplate;
    }

    /**
     * 
     */
    public String getHeaderTemplate() {
        return headerTemplate;
    }

    /**
     * 
     */
    public void setHeaderTemplate(String headerTemplate) {
        this.headerTemplate = headerTemplate;
    }

    /**
     * 
     */
    public String getValidateTemplate() {
        return validateTemplate;
    }

    /**
     * 
     */
    public void setValidateTemplate(String validateTemplate) {
        this.validateTemplate = validateTemplate;
    }

    /**
     * 
     */
    public String getSensitiveFields() {
        return sensitiveFields;
    }

    /**
     * 
     */
    public void setSensitiveFields(String sensitiveFields) {
        this.sensitiveFields = sensitiveFields;
    }

    /**
     * 
     */
    public String getMessageProcessId() {
        return messageProcessId;
    }

    /**
     * 
     */
    public void setMessageProcessId(String messageProcessId) {
        this.messageProcessId = messageProcessId;
    }

    /**
     * 
     */
    public String getAssembleTemplate() {
        return assembleTemplate;
    }

    /**
     * 
     */
    public void setAssembleTemplate(String assembleTemplate) {
        this.assembleTemplate = assembleTemplate;
    }

    /**
     * 
     */
    public String getAssembleSubTemplate() {
        return assembleSubTemplate;
    }

    /**
     * 
     */
    public void setAssembleSubTemplate(String assembleSubTemplate) {
        this.assembleSubTemplate = assembleSubTemplate;
    }

    /**
     * 
     */
    public String getAssembleExtTemplate() {
        return assembleExtTemplate;
    }

    /**
     * 
     */
    public void setAssembleExtTemplate(String assembleExtTemplate) {
        this.assembleExtTemplate = assembleExtTemplate;
    }

    /**
     * 
     */
    public String getErrorTemplate() {
        return errorTemplate;
    }

    /**
     * 
     */
    public void setErrorTemplate(String errorTemplate) {
        this.errorTemplate = errorTemplate;
    }

    /**
     * 
     */
    public String getErrorSubTemplate() {
        return errorSubTemplate;
    }

    /**
     * 
     */
    public void setErrorSubTemplate(String errorSubTemplate) {
        this.errorSubTemplate = errorSubTemplate;
    }

    /**
     * 
     */
    public String getParserClassName() {
        return parserClassName;
    }

    /**
     * 
     */
    public void setParserClassName(String parserClassName) {
        this.parserClassName = parserClassName;
    }

    /**
     * 
     */
    public String getAssembleType() {
        return assembleType;
    }

    /**
     * 
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
