package com.github.loadup.components.gateway.certification.spi;

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
 * 远程调用spi实现，本地满足不了算法需求则走远程调用，需应用自助实现
 */
public interface OuterService {

    /**
     * 签名操作统一接口
     */
    public String sign(String srcContent, CertificationFactor certificationFactor);

    /**
     * 验签操作统一接口
     * <p>
     * <p>
     * <p>
     * param certificationFactor  操作要素
     */
    public boolean verify(String SrcContent, String signedContent,
                        CertificationFactor certificationFactor);

    /**
     * 报文加密统一接口
     * <p>
     * <p>
     * param certificationFactor  操作要素
     */
    public String encrypt(String srcContent, CertificationFactor certificationFactor);

    /**
     * 报文解密统一接口
     * <p>
     * <p>
     * param certificationFactor  操作要素
     */
    public String decrypt(String encryptedContent, CertificationFactor certificationFactor);

    /**
     * 报文摘要统一接口
     */
    public String digest(String srcContent, CertificationFactor certificationFactor);

}
