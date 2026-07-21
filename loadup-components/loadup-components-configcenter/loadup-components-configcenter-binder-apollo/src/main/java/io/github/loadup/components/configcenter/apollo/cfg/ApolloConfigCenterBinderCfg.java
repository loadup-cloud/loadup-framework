package io.github.loadup.components.configcenter.apollo.cfg;

/*-
 * #%L
 * LoadUp ConfigCenter Binder Apollo
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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

    /**
     * Apollo Meta Server address.
     */
    private String meta;

    /**
     * Apollo AppId.
     */
    private String appId;

    /**
     * Environment (DEV / FAT / UAT / PRO).
     */
    private String env = "DEV";

    /**
     * Cluster name.
     */
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
