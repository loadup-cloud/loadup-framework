package io.github.loadup.components.configcenter.apollo.cfg;

import io.github.loadup.components.configcenter.cfg.ConfigCenterBinderCfg;
import lombok.Getter;
import lombok.Setter;

/**
 * Apollo binder 配置。
 *
 * <pre>
 * loadup:
 *   configcenter:
 *     binders:
 *       apollo:
 *         meta: http://apollo-configservice:8080
 *         app-id: my-application
 *         env: DEV
 *         cluster: default
 *         apollo-namespace: application
 * </pre>
 */
@Getter
@Setter
public class ApolloConfigCenterBinderCfg extends ConfigCenterBinderCfg {

    /** Apollo Meta Server 地址。 */
    private String meta;

    /** Apollo AppId。 */
    private String appId;

    /** 环境（DEV / FAT / UAT / PRO）。 */
    private String env = "DEV";

    /** 集群名。 */
    private String cluster = "default";

    /**
     * Apollo Namespace（对应 Apollo 中的配置集名称，区别于 binderCfg.namespace 租户概念）。
     * 默认为 {@code application}。
     */
    private String apolloNamespace = "application";

    @Override
    public Object getIdentity() {
        return appId + "@" + env + "/" + apolloNamespace;
    }
}
