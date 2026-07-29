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
    public static class Contact {
        /** Contact display name. */
        private String name = "LoadUp Team";
        /** Contact URL. */
        private String url = "https://github.com/loadup-cloud";
        /** Contact e-mail. */
        private String email = "";
    }

    /** License sub-properties. */
    public static class License {
        /** License name. */
        private String name = "GPL-3.0";
        /** License URL. */
        private String url = "https://opensource.org/license/gpl-3-0";
    }

    public SpringDocProperties(boolean enabled, String title, String description, String version, boolean jwtEnabled, String jwtSchemeName, Contact contact, License license, String name, String url, String email, String name, String url) {
        this.enabled = enabled;
        this.title = title;
        this.description = description;
        this.version = version;
        this.jwtEnabled = jwtEnabled;
        this.jwtSchemeName = jwtSchemeName;
        this.contact = contact;
        this.license = license;
        this.name = name;
        this.url = url;
        this.email = email;
        this.name = name;
        this.url = url;
    }

    public SpringDocProperties() {
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getVersion() {
        return this.version;
    }

    public boolean isJwtEnabled() {
        return this.jwtEnabled;
    }

    public String getJwtSchemeName() {
        return this.jwtSchemeName;
    }

    public Contact getContact() {
        return this.contact;
    }

    public License getLicense() {
        return this.license;
    }

    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

    public String getEmail() {
        return this.email;
    }

    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setJwtEnabled(boolean jwtEnabled) {
        this.jwtEnabled = jwtEnabled;
    }

    public void setJwtSchemeName(String jwtSchemeName) {
        this.jwtSchemeName = jwtSchemeName;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(enabled, title, description, version, jwtEnabled, jwtSchemeName, contact, license, name, url, email, name, url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpringDocProperties other = (SpringDocProperties) o;
        if (!java.util.Objects.equals(enabled, other.enabled)) return false;
        if (!java.util.Objects.equals(title, other.title)) return false;
        if (!java.util.Objects.equals(description, other.description)) return false;
        if (!java.util.Objects.equals(version, other.version)) return false;
        if (!java.util.Objects.equals(jwtEnabled, other.jwtEnabled)) return false;
        if (!java.util.Objects.equals(jwtSchemeName, other.jwtSchemeName)) return false;
        if (!java.util.Objects.equals(contact, other.contact)) return false;
        if (!java.util.Objects.equals(license, other.license)) return false;
        if (!java.util.Objects.equals(name, other.name)) return false;
        if (!java.util.Objects.equals(url, other.url)) return false;
        if (!java.util.Objects.equals(email, other.email)) return false;
        if (!java.util.Objects.equals(name, other.name)) return false;
        if (!java.util.Objects.equals(url, other.url)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "SpringDocProperties(" + "enabled=" + enabled + ", " + "title=" + title + ", " + "description=" + description + ", " + "version=" + version + ", " + "jwtEnabled=" + jwtEnabled + ", " + "jwtSchemeName=" + jwtSchemeName + ", " + "contact=" + contact + ", " + "license=" + license + ", " + "name=" + name + ", " + "url=" + url + ", " + "email=" + email + ", " + "name=" + name + ", " + "url=" + url + ")";
    }
}
