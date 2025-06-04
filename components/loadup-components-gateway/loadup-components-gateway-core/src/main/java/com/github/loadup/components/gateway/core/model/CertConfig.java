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

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 证书的存储模型
 */
public class CertConfig {

    /**
     * cert唯一标记
     */
    private String certCode;

    /**
     * 证书类型
     */
    private String certType;

    /**
     * 证书内容，统一以 byte 数组base64存储
     */
    private String certContent;

    /**
     * 证书状态
     */
    private String certStatus;

    /**
     * 有效期开始
     */
    private Date gmtValid;

    /**
     * 失效日期
     */
    private Date gmtInValid;

    /**
     * 证书内容类型：REAL_CONTENT OR CERT Alias
     */
    private String certContentType;

    /**
     * 证书特殊属性
     */
    private String certSpecial;

    /**
     * 证书唯一Id，用于drm更新证书时使用
     */
    private String certUniqId;

    /**
     * client id
     */
    private String clientId;

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
    public String getCertContent() {
        return certContent;
    }

    /**
     *
     */
    public void setCertContent(String certContent) {
        this.certContent = certContent;
    }

    /**
     *
     */
    public String getCertSpecial() {
        return certSpecial;
    }

    /**
     *
     */
    public void setCertSpecial(String certSpecial) {
        this.certSpecial = certSpecial;
    }

    /**
     *
     */
    public String getCertStatus() {
        return certStatus;
    }

    /**
     *
     */
    public void setCertStatus(String certStatus) {
        this.certStatus = certStatus;
    }

    /**
     *
     */
    public String getCertType() {
        return certType;
    }

    /**
     *
     */
    public void setCertType(String certType) {
        this.certType = certType;
    }

    /**
     *
     */
    public String getCertContentType() {
        return certContentType;
    }

    /**
     *
     */
    public void setCertContentType(String certContentType) {
        this.certContentType = certContentType;
    }

    /**
     *
     */
    public String getCertUniqId() {
        return certUniqId;
    }

    /**
     *
     */
    public void setCertUniqId(String certUniqId) {
        this.certUniqId = certUniqId;
    }

    /**
     *
     */
    public Date getGmtInValid() {
        return gmtInValid;
    }

    /**
     *
     */
    public void setGmtInValid(Date gmtInValid) {
        this.gmtInValid = gmtInValid;
    }

    /**
     *
     */
    public Date getGmtValid() {
        return gmtValid;
    }

    /**
     *
     */
    public void setGmtValid(Date gmtValid) {
        this.gmtValid = gmtValid;
    }

    /**
     *
     */
    public String getClientId() {
        return clientId;
    }

    /**
     *
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * toString用于log记录
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
