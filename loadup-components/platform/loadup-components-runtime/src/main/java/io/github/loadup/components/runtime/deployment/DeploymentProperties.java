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
package io.github.loadup.components.runtime.deployment;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Deployment settings of one deployable application.
 *
 * <p>A deployment serves exactly one tenant, so this type carries the deployment code only. It
 * knows nothing about database schemas, JDBC connections or actor identity.
 */
@ConfigurationProperties(prefix = "loadup.deployment")
public class DeploymentProperties {

    /** Canonical deployment code format: bounded, lowercase and request-comparable. */
    public static final String CODE_PATTERN = "[a-z][a-z0-9-]{0,63}";

    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
