package io.github.loadup.components.configcenter.nacos.cfg;

import io.github.loadup.components.configcenter.cfg.ConfigCenterBinderCfg;
import lombok.Getter;
import lombok.Setter;

/**
 * Nacos binder 配置。
 *
 * <pre>
 * loadup:
 *   configcenter:
 *     binders:
 *       nacos:
 *         server-addr: 127.0.0.1:8848
 *         namespace: public
 *         username: nacos
 *         password: nacos
 *         timeout: 3000
 * </pre>
 */
@Getter
@Setter
public class NacosConfigCenterBinderCfg extends ConfigCenterBinderCfg {

    /** Nacos 服务端地址，多个用逗号分隔，如 {@code 127.0.0.1:8848}。 */
    private String serverAddr = "127.0.0.1:8848";

    /** Nacos 用户名（2.x 鉴权模式）。 */
    private String username;

    /** Nacos 密码（2.x 鉴权模式）。 */
    private String password;

    /** Nacos 访问 Token（bearer token，可选）。 */
    private String accessToken;

    /** 获取配置超时时间（毫秒）。 */
    private long timeout = 3000L;

    @Override
    public Object getIdentity() {
        return serverAddr + "@" + getNamespace();
    }
}
