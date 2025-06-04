/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.certification.model;

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

import org.apache.commons.lang3.StringUtils;

/**
 * 证书内容类型枚举
 */
public enum CertContentType {

    /**
     * 证书内容
     */
    CERT_OFFICIAL_CONTENT("CERT_OFFICIAL_CONTENT", "证书正式内容"),

    /**
     * 证书别名
     */
    CERT_ALIAS_NAME("CERT_ALIAS_NAME", "证书别名"),

    /**
     * kmiKey别名
     */
    KMI_KEY_ALIAS_NAME("KMI_KEY_ALIAS_NAME", "kmiKey别名"),
    ;

    /**
     * 证书内容类型
     */
    private String certContentType;

    /**
     * 描述
     */
    private String desc;

    /**
     * 构造函数
     */
    CertContentType(String certContentType, String desc) {
        this.certContentType = certContentType;
        this.desc = desc;
    }

    /**
     * 基于String类型获取枚举
     */
    public static CertContentType getByName(String certContentType) {
        for (CertContentType contentType : CertContentType.values()) {
            if (StringUtils.equals(certContentType, contentType.getCertContentType())) {
                return contentType;
            }
        }
        return null;
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
    public String getDesc() {
        return desc;
    }

    /**
     *
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
