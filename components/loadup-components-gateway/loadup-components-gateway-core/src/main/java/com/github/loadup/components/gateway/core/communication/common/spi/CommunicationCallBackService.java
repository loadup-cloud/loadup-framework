package com.github.loadup.components.gateway.core.communication.common.spi;

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
    public MessageEnvelope receive(String transUUID, CommunicationConfig config,
                                   MessageEnvelope messageEnvelope);

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
