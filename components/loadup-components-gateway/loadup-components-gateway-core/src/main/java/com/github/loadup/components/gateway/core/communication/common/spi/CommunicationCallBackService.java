/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.core.communication.common.spi;

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

import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;

/**
 * 报文接收回调处理器，框架通过该接口来实现该消息接收入口的业务处理过程
 */
public interface CommunicationCallBackService {

    /**
     * 获取服务端配置
     */
    public CommunicationConfig getServerConfig(String uri);

    /**
     * 报文接收后回调处理
     */
    public MessageEnvelope receive(String transUUID, CommunicationConfig config, MessageEnvelope messageEnvelope);

    /**
     * 获取ssl私钥证书
     */
    public String getSslPrivateCert(String appId);

    /**
     * 获取ssl私钥证书密码
     */
    public String getSslPrivateCertPwd(String appId);

    /**
     * 获取ssl根证书
     */
    public String getSslRootCert(String appId);

    /**
     * 获取ssl根证书文件密码
     */
    public String getSslRootCertPwd(String appId);

    /**
     * 获取ssl根证书密钥密码
     */
    public String getSslRootCertKeyPwd(String appId);
}
