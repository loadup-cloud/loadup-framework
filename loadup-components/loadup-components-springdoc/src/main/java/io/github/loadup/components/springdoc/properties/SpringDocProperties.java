package io.github.loadup.components.springdoc.properties;

/*-
 * #%L
 * LoadUp Components SpringDoc
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import lombok.Data;
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
 *       name: GPL-3.0
 *       url: https://opensource.org/license/gpl-3-0
 * }</pre>
 */
@Data
@ConfigurationProperties(prefix = "loadup.springdoc")
public class SpringDocProperties {

    /** Whether the SpringDoc / knife4j auto-configuration is enabled. */
    private boolean enabled = true;

    /** OpenAPI {@code info.title}. */
    private String title = "LoadUp API";

    /** OpenAPI {@code info.description}. */
    private String description = "LoadUp Framework REST API documentation.";

    /** OpenAPI {@code info.version}. */
    private String version = "0.0.2-SNAPSHOT";

    /** Whether to register a global JWT Bearer security scheme. */
    private boolean jwtEnabled = true;

    /** Name of the JWT security scheme registered in OpenAPI components. */
    private String jwtSchemeName = "BearerAuth";

    /** Contact information shown in the API documentation. */
    private Contact contact = new Contact();

    /** License information shown in the API documentation. */
    private License license = new License();

    /** Contact sub-properties. */
    @Data
    public static class Contact {
        /** Contact display name. */
        private String name = "LoadUp Team";
        /** Contact URL. */
        private String url = "https://github.com/loadup-cloud";
        /** Contact e-mail. */
        private String email = "";
    }

    /** License sub-properties. */
    @Data
    public static class License {
        /** License name. */
        private String name = "GPL-3.0";
        /** License URL. */
        private String url = "https://opensource.org/license/gpl-3-0";
    }
}

