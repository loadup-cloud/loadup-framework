package io.github.loadup.components.springdoc.properties;

/*-
 * #%L
 * LoadUp Components SpringDoc
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the LoadUp SpringDoc / knife4j component.
 *
 * <p>All properties are prefixed with {@code loadup.springdoc}. Example:
 *
 * <pre>{@code
 * loadup:
 *   springdoc:
 *     title: My API
 *     description: My service REST API documentation
 *     version: 1.0.0
 *     jwt-enabled: true
 *     contact:
 *       name: Dev Team
 *       url: https://example.com
 *       email: dev@example.com
 *     license:
 *       name: Apache-2.0
 *       url: https://www.apache.org/licenses/LICENSE-2.0.txt
 * }</pre>
 */
@ConfigurationProperties(prefix = "loadup.springdoc")
public class SpringDocProperties {

    /**
     * Whether the SpringDoc / knife4j auto-configuration is enabled.
     */
    private boolean enabled = true;

    /**
     * OpenAPI {@code info.title}.
     */
    private String title = "LoadUp API";

    /**
     * OpenAPI {@code info.description}.
     */
    private String description = "LoadUp Framework REST API documentation.";

    /**
     * OpenAPI {@code info.version}.
     */
    private String version = "0.0.2-SNAPSHOT";

    /**
     * Whether to register a global JWT Bearer security scheme.
     */
    private boolean jwtEnabled = true;

    /**
     * Name of the JWT security scheme registered in OpenAPI components.
     */
    private String jwtSchemeName = "BearerAuth";

    /**
     * Contact information shown in the API documentation.
     */
    private Contact contact = new Contact();

    /**
     * License information shown in the API documentation.
     */
    private License license = new License();

    /**
     * Contact sub-properties.
     */
    public static class Contact {
        /**
         * Contact display name.
         */
        private String name = "LoadUp Team";
        /**
         * Contact URL.
         */
        private String url = "https://github.com/loadup-cloud";
        /**
         * Contact e-mail.
         */
        private String email = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    /**
     * License sub-properties.
     */
    public static class License {
        /**
         * License name.
         */
        private String name = "Apache-2.0";
        /**
         * License URL.
         */
        private String url = "https://www.apache.org/licenses/LICENSE-2.0.txt";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isJwtEnabled() {
        return jwtEnabled;
    }

    public void setJwtEnabled(boolean jwtEnabled) {
        this.jwtEnabled = jwtEnabled;
    }

    public String getJwtSchemeName() {
        return jwtSchemeName;
    }

    public void setJwtSchemeName(String jwtSchemeName) {
        this.jwtSchemeName = jwtSchemeName;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }
}
