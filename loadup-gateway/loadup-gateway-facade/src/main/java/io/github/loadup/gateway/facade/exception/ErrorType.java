package io.github.loadup.gateway.facade.exception;

/*-
 * #%L
 * LoadUp Gateway Facade
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

/**
 * Error type enumeration
 */
public enum ErrorType {

    /**
     * Configuration error
     */
    CONFIGURATION("Configuration error"),

    /**
     * Routing error
     */
    ROUTING("Routing error"),

    /**
     * Plugin error
     */
    PLUGIN("Plugin error"),

    /**
     * Proxy error
     */
    PROXY("Proxy error"),

    /**
     * Parameter validation error
     */
    VALIDATION("Validation error"),

    /**
     * Business logic error
     */
    BUSINESS("Business logic error"),

    /**
     * System error
     */
    SYSTEM("System error"),

    /**
     * Network error
     */
    NETWORK("Network error"),

    /**
     * Serialization/Deserialization error
     */
    SERIALIZATION("Serialization error"),

    /**
     * Template error
     */
    TEMPLATE("Template error"),

    /**
     * Storage error
     */
    STORAGE("Storage error"),

    /**
     * Security error
     */
    SECURITY("Security error"),
    AUTHORIZATION("Authorization error"),
    RATE_LIMIT("Rate limit error"),
    TIMEOUT("Timeout error"),
    UNKNOWN("Unknown error");

    private final String description;

    ErrorType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name();
    }
}
