package io.github.loadup.components.configcenter.model;

/*-
 * #%L
 * LoadUp ConfigCenter Components API
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import java.time.Instant;

/**
 * Wrapper that holds a single configuration entry retrieved from the config center.
 */
public class ConfigEntry {

    /**
     * Config item ID (key).
     */
    private String dataId;

    /**
     * Config group.
     */
    private String group;

    /**
     * Namespace / tenant.
     */
    private String namespace;

    /**
     * Config content (plain text).
     */
    private String content;

    /**
     * Content type, e.g. yaml / properties / json / text.
     */
    private String contentType;

    /**
     * Version returned by the config center; null if not supported.
     */
    private String version;

    /**
     * Last-modified timestamp returned by the config center; null if not supported.
     */
    private Instant lastModified;

    public ConfigEntry(
            String dataId,
            String group,
            String namespace,
            String content,
            String contentType,
            String version,
            Instant lastModified) {
        this.dataId = dataId;
        this.group = group;
        this.namespace = namespace;
        this.content = content;
        this.contentType = contentType;
        this.version = version;
        this.lastModified = lastModified;
    }

    public ConfigEntry() {}

    public String getDataId() {
        return this.dataId;
    }

    public String getGroup() {
        return this.group;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getContent() {
        return this.content;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getVersion() {
        return this.version;
    }

    public Instant getLastModified() {
        return this.lastModified;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(dataId, group, namespace, content, contentType, version, lastModified);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigEntry other = (ConfigEntry) o;
        if (!java.util.Objects.equals(dataId, other.dataId)) return false;
        if (!java.util.Objects.equals(group, other.group)) return false;
        if (!java.util.Objects.equals(namespace, other.namespace)) return false;
        if (!java.util.Objects.equals(content, other.content)) return false;
        if (!java.util.Objects.equals(contentType, other.contentType)) return false;
        if (!java.util.Objects.equals(version, other.version)) return false;
        if (!java.util.Objects.equals(lastModified, other.lastModified)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "ConfigEntry(" + "dataId=" + dataId + ", " + "group=" + group + ", " + "namespace=" + namespace + ", "
                + "content=" + content + ", " + "contentType=" + contentType + ", " + "version=" + version + ", "
                + "lastModified=" + lastModified + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String dataId;
        private String group;
        private String namespace;
        private String content;
        private String contentType;
        private String version;
        private Instant lastModified;

        public Builder dataId(String dataId) {
            this.dataId = dataId;
            return this;
        }

        public Builder group(String group) {
            this.group = group;
            return this;
        }

        public Builder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder lastModified(Instant lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public ConfigEntry build() {
            return new ConfigEntry(
                    this.dataId,
                    this.group,
                    this.namespace,
                    this.content,
                    this.contentType,
                    this.version,
                    this.lastModified);
        }
    }
}
