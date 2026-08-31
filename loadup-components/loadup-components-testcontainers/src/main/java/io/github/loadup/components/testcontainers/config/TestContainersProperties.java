package io.github.loadup.components.testcontainers.config;

/*-
 * #%L
 * Loadup Components TestContainers
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(TestContainersProperties.class);

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

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isReusable() {
            return reusable;
        }

        public void setReusable(boolean reusable) {
            this.reusable = reusable;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ContainerConfig getMysql() {
        return mysql;
    }

    public void setMysql(ContainerConfig mysql) {
        this.mysql = mysql;
    }

    public ContainerConfig getPostgresql() {
        return postgresql;
    }

    public void setPostgresql(ContainerConfig postgresql) {
        this.postgresql = postgresql;
    }

    public ContainerConfig getMongodb() {
        return mongodb;
    }

    public void setMongodb(ContainerConfig mongodb) {
        this.mongodb = mongodb;
    }

    public ContainerConfig getRedis() {
        return redis;
    }

    public void setRedis(ContainerConfig redis) {
        this.redis = redis;
    }

    public ContainerConfig getKafka() {
        return kafka;
    }

    public void setKafka(ContainerConfig kafka) {
        this.kafka = kafka;
    }

    public ContainerConfig getElasticsearch() {
        return elasticsearch;
    }

    public void setElasticsearch(ContainerConfig elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

    public ContainerConfig getLocalstack() {
        return localstack;
    }

    public void setLocalstack(ContainerConfig localstack) {
        this.localstack = localstack;
    }

    public boolean isReusable() {
        return reusable;
    }

    public void setReusable(boolean reusable) {
        this.reusable = reusable;
    }
}
