/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.certification.manager;

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

import com.github.loadup.components.gateway.certification.model.CertificationFactor;

/**
 * 算法管理接口(报文类)
 */
public interface AlgoManager {

    /**
     * 加密接口
     */
    public String encrypt(String srcContent, CertificationFactor certificationFactor);

    /**
     * 解密接口
     */
    public String decrypt(String encryptedContent, CertificationFactor certificationFactor);

    /**
     * 加签接口
     */
    public String sign(String srcContent, CertificationFactor certificationFactor);

    /**
     * 验签接口
     */
    public boolean verify(String SrcContent, String signedContent, CertificationFactor certificationFactor);

    /**
     * 摘要接口
     */
    public String digest(String srcContent, CertificationFactor certificationFactor);

    /**
     * 特殊加签接口
     */
    public String encode(String srcContent, CertificationFactor certificationFactor);

    /**
     * 特殊验签接口
     */
    public String decode(String SrcContent, String signedContent, CertificationFactor certificationFactor);
}
