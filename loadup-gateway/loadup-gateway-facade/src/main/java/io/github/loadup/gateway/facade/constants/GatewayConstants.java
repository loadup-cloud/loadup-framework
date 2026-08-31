package io.github.loadup.gateway.facade.constants;

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
 * Gateway constant definitions
 */
public final class GatewayConstants {

    private GatewayConstants() {}

    /**
     * Protocol types
     */
    public static final class Protocol {
        public static final String HTTP = "HTTP";
        public static final String RPC = "RPC";
        public static final String BEAN = "BEAN";
    }

    /**
     * Storage types
     */
    public static final class Storage {
        public static final String FILE = "FILE";
        public static final String DATABASE = "DATABASE";
    }

    /**
     * Template types
     */
    public static final class Template {
        public static final String REQUEST = "REQUEST";
        public static final String RESPONSE = "RESPONSE";
    }

    /**
     * Content types
     */
    public static final class ContentType {
        public static final String JSON = "application/json;charset=UTF-8";
        public static final String FORM = "application/x-www-form-urlencoded";
        public static final String XML = "application/xml";
    }

    /**
     * HTTP methods
     */
    public static final class HttpMethod {
        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String PUT = "PUT";
        public static final String DELETE = "DELETE";
        public static final String PATCH = "PATCH";
    }

    /**
     * Status codes
     */
    public static final class Status {
        public static final int SUCCESS = 200;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int INTERNAL_ERROR = 500;
        public static final int SERVICE_UNAVAILABLE = 503;
    }

    /**
     * Configuration keys
     */
    public static final class Config {
        public static final String GATEWAY_PREFIX = "loadup.gateway";
        public static final String PLUGIN_ENABLED = "enabled";
        public static final String PLUGIN_CONFIG = "config";
        public static final String TEMPLATE_PATH = "template.path";
        public static final String STORAGE_TYPE = "storage.type";
    }

    /**
     * Route configuration property keys
     */
    public static final class PropertyKeys {
        public static final String TIMEOUT = "timeout";
        public static final String RETRY_COUNT = "retryCount";
        public static final String WRAP_RESPONSE = "wrapResponse";
    }
}
