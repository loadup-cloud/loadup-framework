/* Copyright (C) LoadUp Cloud 2022-2025 */
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
     *
     */
    public String getAlgoName() {
        return algoName;
    }

    /**
     *
     */
    public void setAlgoName(String algoName) {
        this.algoName = algoName;
    }

    /**
     *
     */
    public String getAlgoProperties() {
        return algoProperties;
    }

    /**
     *
     */
    public void setAlgoProperties(String algoProperties) {
        this.algoProperties = algoProperties;
    }

    /**
     *
     */
    public String getCertCode() {
        return certCode;
    }

    /**
     *
     */
    public void setCertCode(String certCode) {
        this.certCode = certCode;
    }

    /**
     *
     */
    public String getCertTypes() {
        return certTypes;
    }

    /**
     *
     */
    public void setCertTypes(String certTypes) {
        this.certTypes = certTypes;
    }

    /**
     *
     */
    public String getMemo() {
        return memo;
    }

    /**
     *
     */
    public void setMemo(String memo) {
        this.memo = memo;
    }

    /**
     *
     */
    public String getOperateType() {
        return operateType;
    }

    /**
     *
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
