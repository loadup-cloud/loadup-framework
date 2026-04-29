package io.github.loadup.components.configcenter.nacos.cfg;

import io.github.loadup.components.configcenter.cfg.ConfigCenterBinderCfg;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for the Nacos binder.
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

    /** Nacos server address; multiple addresses separated by commas, e.g. {@code 127.0.0.1:8848}. */
    private String serverAddr = "127.0.0.1:8848";

    /** Nacos username (for 2.x auth mode). */
    private String username;

    /** Nacos password (for 2.x auth mode). */
    private String password;

    /** Nacos access token (bearer token, optional). */
    private String accessToken;

    /** Config fetch timeout in milliseconds. */
    private long timeout = 3000L;

    @Override
    public Object getIdentity() {
        return serverAddr + "@" + getNamespace();
    }
}
