package io.github.loadup.components.configcenter.apollo.cfg;

import io.github.loadup.components.configcenter.cfg.ConfigCenterBinderCfg;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for the Apollo binder.
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

    /** Apollo Meta Server address. */
    private String meta;

    /** Apollo AppId. */
    private String appId;

    /** Environment (DEV / FAT / UAT / PRO). */
    private String env = "DEV";

    /** Cluster name. */
    private String cluster = "default";

    /**
     * Apollo Namespace (corresponds to a config-set name in Apollo;
     * distinct from the tenant {@code binderCfg.namespace} concept).
     * Defaults to {@code application}.
     */
    private String apolloNamespace = "application";

    @Override
    public Object getIdentity() {
        return appId + "@" + env + "/" + apolloNamespace;
    }
}
