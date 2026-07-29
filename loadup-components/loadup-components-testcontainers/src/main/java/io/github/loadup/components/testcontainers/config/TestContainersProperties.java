package io.github.loadup.components.testcontainers.config;

/*-
 * #%L
 * Loadup Components TestContainers
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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for TestContainers.
 *
 * <p>This class provides centralized configuration for all TestContainers, allowing flexible
 * switching between TestContainers and real services.
 *
 * <p>Usage example in application.yml:
 *
 * <pre>
 * loadup:
 *   testcontainers:
 *     enabled: true
 *     mysql:
 *       enabled: true
 *       version: mysql:8.0
 *     redis:
 *       enabled: false  # Use real Redis
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "loadup.testcontainers")
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class TestContainersProperties {

    /**
     * Global switch to enable/disable all TestContainers (default: true)
     */
    private boolean enabled = true;

    /**
     * MySQL container configuration
     */
    private ContainerConfig mysql = new ContainerConfig();

    /**
     * PostgreSQL container configuration
     */
    private ContainerConfig postgresql = new ContainerConfig();

    /**
     * MongoDB container configuration
     */
    private ContainerConfig mongodb = new ContainerConfig();

    /**
     * Redis container configuration
     */
    private ContainerConfig redis = new ContainerConfig();

    /**
     * Kafka container configuration
     */
    private ContainerConfig kafka = new ContainerConfig();

    /**
     * Elasticsearch container configuration
     */
    private ContainerConfig elasticsearch = new ContainerConfig();

    /**
     * LocalStack container configuration
     */
    private ContainerConfig localstack = new ContainerConfig();

    private boolean reusable;

    /**
     * Configuration for a specific container type.
     *
     * @author LoadUp Framework
     * @since 1.0.0
     */
    public static class ContainerConfig {
        /**
         * Enable this specific container (default: false, must be explicitly enabled)
         */
        private boolean enabled = false;

        /**
         * Docker image version (e.g., "mysql:8.0")
         */
        private String image;

        /**
         * Database name (for database containers)
         */
        private String database;

        /**
         * Username (for services requiring authentication)
         */
        private String username;

        /**
         * Password (for services requiring authentication)
         */
        private String password;

        /**
         * 开启复用，默认为 true。极大提升本地多次运行测试的速度。
         */
        private boolean reusable = true;
    }

    public provides(boolean enabled, ContainerConfig mysql, ContainerConfig postgresql, ContainerConfig mongodb, ContainerConfig redis, ContainerConfig kafka, ContainerConfig elasticsearch, ContainerConfig localstack, boolean reusable, boolean enabled, String image, String database, String username, String password, boolean reusable) {
        this.enabled = enabled;
        this.mysql = mysql;
        this.postgresql = postgresql;
        this.mongodb = mongodb;
        this.redis = redis;
        this.kafka = kafka;
        this.elasticsearch = elasticsearch;
        this.localstack = localstack;
        this.reusable = reusable;
        this.enabled = enabled;
        this.image = image;
        this.database = database;
        this.username = username;
        this.password = password;
        this.reusable = reusable;
    }

    public provides() {
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public ContainerConfig getMysql() {
        return this.mysql;
    }

    public ContainerConfig getPostgresql() {
        return this.postgresql;
    }

    public ContainerConfig getMongodb() {
        return this.mongodb;
    }

    public ContainerConfig getRedis() {
        return this.redis;
    }

    public ContainerConfig getKafka() {
        return this.kafka;
    }

    public ContainerConfig getElasticsearch() {
        return this.elasticsearch;
    }

    public ContainerConfig getLocalstack() {
        return this.localstack;
    }

    public boolean isReusable() {
        return this.reusable;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public String getImage() {
        return this.image;
    }

    public String getDatabase() {
        return this.database;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean isReusable() {
        return this.reusable;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setMysql(ContainerConfig mysql) {
        this.mysql = mysql;
    }

    public void setPostgresql(ContainerConfig postgresql) {
        this.postgresql = postgresql;
    }

    public void setMongodb(ContainerConfig mongodb) {
        this.mongodb = mongodb;
    }

    public void setRedis(ContainerConfig redis) {
        this.redis = redis;
    }

    public void setKafka(ContainerConfig kafka) {
        this.kafka = kafka;
    }

    public void setElasticsearch(ContainerConfig elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

    public void setLocalstack(ContainerConfig localstack) {
        this.localstack = localstack;
    }

    public void setReusable(boolean reusable) {
        this.reusable = reusable;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setReusable(boolean reusable) {
        this.reusable = reusable;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(enabled, mysql, postgresql, mongodb, redis, kafka, elasticsearch, localstack, reusable, enabled, image, database, username, password, reusable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        provides other = (provides) o;
        if (!java.util.Objects.equals(enabled, other.enabled)) return false;
        if (!java.util.Objects.equals(mysql, other.mysql)) return false;
        if (!java.util.Objects.equals(postgresql, other.postgresql)) return false;
        if (!java.util.Objects.equals(mongodb, other.mongodb)) return false;
        if (!java.util.Objects.equals(redis, other.redis)) return false;
        if (!java.util.Objects.equals(kafka, other.kafka)) return false;
        if (!java.util.Objects.equals(elasticsearch, other.elasticsearch)) return false;
        if (!java.util.Objects.equals(localstack, other.localstack)) return false;
        if (!java.util.Objects.equals(reusable, other.reusable)) return false;
        if (!java.util.Objects.equals(enabled, other.enabled)) return false;
        if (!java.util.Objects.equals(image, other.image)) return false;
        if (!java.util.Objects.equals(database, other.database)) return false;
        if (!java.util.Objects.equals(username, other.username)) return false;
        if (!java.util.Objects.equals(password, other.password)) return false;
        if (!java.util.Objects.equals(reusable, other.reusable)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "provides(" + "enabled=" + enabled + ", " + "mysql=" + mysql + ", " + "postgresql=" + postgresql + ", " + "mongodb=" + mongodb + ", " + "redis=" + redis + ", " + "kafka=" + kafka + ", " + "elasticsearch=" + elasticsearch + ", " + "localstack=" + localstack + ", " + "reusable=" + reusable + ", " + "enabled=" + enabled + ", " + "image=" + image + ", " + "database=" + database + ", " + "username=" + username + ", " + "password=" + password + ", " + "reusable=" + reusable + ")";
    }
}
