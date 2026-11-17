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
package io.github.loadup.components.webmvc;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Request-entry policy for the deployment code header.
 *
 * <p>The header is required for every request except preflight requests and the explicitly listed
 * paths below. Exemptions are per application and per protocol endpoint; there is no blanket
 * exemption by login state, callback category or path family.
 */
@ConfigurationProperties(prefix = "loadup.webmvc.deployment-code")
public class DeploymentCodeProperties {

    private static final Pattern HEADER_NAME = Pattern.compile("[A-Za-z0-9!#$%&'*+.^_`|~-]{1,128}");
    private static final Pattern PATH = Pattern.compile("/[A-Za-z0-9._~!$&'()*+,;=:@/{}*-]*");

    private String headerName = "X-Tenant-Code";

    /** Ant patterns exempted from the header check: probes, metrics and fixed static entries. */
    private List<String> excludedPaths = List.of(
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",
            "/actuator/prometheus",
            "/error",
            "/",
            "/index.html",
            "/favicon.ico",
            "/assets/**",
            "/static/**");

    public DeploymentCodeProperties() {}

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        if (headerName == null || !HEADER_NAME.matcher(headerName).matches()) {
            throw new IllegalArgumentException("Deployment code header name is not a valid HTTP token");
        }
        this.headerName = headerName;
    }

    public List<String> getExcludedPaths() {
        return excludedPaths;
    }

    public void setExcludedPaths(List<String> excludedPaths) {
        if (excludedPaths == null) {
            this.excludedPaths = List.of();
            return;
        }
        List<String> normalized = excludedPaths.stream()
                .map(path -> Objects.requireNonNull(path, "excluded path"))
                .map(DeploymentCodeProperties::requirePath)
                .toList();
        this.excludedPaths = normalized;
    }

    private static String requirePath(String path) {
        if (!PATH.matcher(path).matches() || path.contains("//") || path.contains("..")) {
            throw new IllegalArgumentException("Excluded deployment code path must be an absolute path: " + path);
        }
        return path;
    }
}
