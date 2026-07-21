package io.github.loadup.components.springdoc.properties;

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

