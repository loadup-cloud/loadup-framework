/*
 * Copyright (C) 2026 LoadUp Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.loadup.components.configcenter.xxlconf;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@ConfigurationProperties("loadup.configcenter.binder.xxl-conf")
public record XxlConfConfigCenterProperties(URI address, String accessToken, String appName, String env) {
    public XxlConfConfigCenterProperties {
        Assert.isTrue(
                address != null
                        && address.getHost() != null
                        && ("https".equalsIgnoreCase(address.getScheme())
                                || "http".equalsIgnoreCase(address.getScheme()))
                        && address.getUserInfo() == null
                        && address.getQuery() == null
                        && address.getFragment() == null,
                "XXL-Conf address must be an HTTP(S) service base URI");
        Assert.isTrue(StringUtils.hasText(accessToken), "XXL-Conf access-token is required");
        Assert.isTrue(StringUtils.hasText(appName), "XXL-Conf app-name is required");
        Assert.isTrue(StringUtils.hasText(env), "XXL-Conf env is required");
    }

    @Override
    public String toString() {
        return "XxlConfConfigCenterProperties[appName=" + appName + ", env=" + env + "]";
    }
}
