package io.github.loadup.components.configcenter.nacos.cfg;

/*-
 * #%L
 * LoadUp ConfigCenter Binder Nacos
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
public class NacosConfigCenterBinderCfg extends ConfigCenterBinderCfg {

    /**
     * Nacos server address; multiple addresses separated by commas, e.g. {@code 127.0.0.1:8848}.
     */
    private String serverAddr = "127.0.0.1:8848";

    /**
     * Nacos username (for 2.x auth mode).
     */
    private String username;

    /**
     * Nacos password (for 2.x auth mode).
     */
    private String password;

    /**
     * Nacos access token (bearer token, optional).
     */
    private String accessToken;

    /**
     * Config fetch timeout in milliseconds.
     */
    private long timeout = 3000L;

    @Override
    public Object getIdentity() {
        return serverAddr + "@" + getNamespace();
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
