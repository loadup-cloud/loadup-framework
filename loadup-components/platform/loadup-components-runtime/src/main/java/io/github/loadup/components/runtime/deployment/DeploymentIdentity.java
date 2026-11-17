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

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable deployment code of the running process.
 *
 * <p>The value is a configuration constant. It is never the "current tenant", is never stored in
 * an execution context, and never selects a database connection, schema, identity or permission.
 * Request entry points compare an inbound header against this constant; infrastructure pieces that
 * need a stable namespace may read it.
 */
public record DeploymentIdentity(String code) {

    private static final Pattern CODE = Pattern.compile(DeploymentProperties.CODE_PATTERN);

    public DeploymentIdentity {
        Objects.requireNonNull(code, "deployment code");
        if (!CODE.matcher(code).matches()) {
            throw new IllegalArgumentException(
                    "Deployment code must match " + DeploymentProperties.CODE_PATTERN + " but was " + code);
        }
    }

    /** Exact comparison; surrounding whitespace, different case and empty values never match. */
    public boolean matches(String candidate) {
        return Objects.equals(code, candidate);
    }

    public boolean isBlank() {
        return code.isBlank();
    }
}
