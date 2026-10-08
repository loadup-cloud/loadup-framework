/*
 * #%L
 * LoadUp Kms
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
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
 * #L%
 */
package io.github.loadup.components.kms;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** No token or key material is stored in ordinary configuration. */
@ConfigurationProperties("loadup.kms")
public record KmsProperties(
        URI endpoint,
        String mount,
        String namespace,
        String tokenEnv,
        String managementTokenEnv,
        boolean allowInsecureHttp,
        Duration connectTimeout,
        Duration readTimeout,
        String sslBundle,
        Integer maxInputBytes) {
    public KmsProperties {
        mount = mount == null ? "transit" : mount;
        tokenEnv = tokenEnv == null ? "OPENBAO_TOKEN" : tokenEnv;
        managementTokenEnv = managementTokenEnv == null ? "OPENBAO_MANAGEMENT_TOKEN" : managementTokenEnv;
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(3) : connectTimeout;
        readTimeout = readTimeout == null ? Duration.ofSeconds(10) : readTimeout;
        maxInputBytes = maxInputBytes == null ? 65536 : maxInputBytes;
        if (endpoint == null
                || endpoint.getHost() == null
                || endpoint.getUserInfo() != null
                || endpoint.getQuery() != null
                || endpoint.getFragment() != null
                || !("https".equals(endpoint.getScheme()) || (allowInsecureHttp && "http".equals(endpoint.getScheme())))
                || !(endpoint.getPath().isEmpty() || "/".equals(endpoint.getPath()))) {
            throw new IllegalArgumentException(
                    "KMS endpoint requires HTTPS and an origin URL; HTTP is opt-in for development");
        }
        if ("/".equals(endpoint.getPath()))
            endpoint = URI.create(
                    endpoint.toString().substring(0, endpoint.toString().length() - 1));
        if (!mount.matches("[A-Za-z0-9_-]+(/[A-Za-z0-9_-]+)*")
                || mount.length() > 128
                || !tokenEnv.matches("[A-Za-z_][A-Za-z0-9_]*")
                || !managementTokenEnv.matches("[A-Za-z_][A-Za-z0-9_]*")
                || maxInputBytes < 1
                || maxInputBytes > 1024 * 1024
                || (namespace != null
                        && (namespace.isBlank() || namespace.indexOf('\r') >= 0 || namespace.indexOf('\n') >= 0))) {
            throw new IllegalArgumentException("Invalid KMS settings");
        }
    }
}
